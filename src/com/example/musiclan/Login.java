package com.example.musiclan;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
 
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
 
import library.DatabaseHandler;
import library.UserFunctions;

public class Login extends Activity {
	
	Button btnLogin;
    Button btnLinkToRegister;
    EditText inputEmail;
    EditText inputPassword;
    TextView loginErrorMsg;
 
    // JSON Response node names
    private static String KEY_SUCCESS = "success";
    private static String KEY_ERROR = "error";
    private static String KEY_ERROR_MSG = "error_msg";
    private static String KEY_UID = "uid";
    private static String KEY_NAME = "name";
    private static String KEY_EMAIL = "email";
    private static String KEY_CREATED_AT = "created_at";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		inputEmail = (EditText) findViewById(R.id.loginEmail);
        inputPassword = (EditText) findViewById(R.id.loginPassword);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);
        loginErrorMsg = (TextView) findViewById(R.id.login_error);
 
        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View view) {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
               // UserFunctions userFunction = new UserFunctions();
                //JSONObject json = 	
                
                
              /*  Intent dashboard = new Intent(getApplicationContext(), Dashboard.class);
                dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(dashboard);*/
                
                
                //uncomment the following line later
                		new getResponseLogin().execute(email,password);
                		
                		
                		//userFunction.loginUser(email, password);
 
                // check for login response
               
            }
        });
 
        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),  Register.class);
                startActivity(i);
                finish();
            }
        });
    
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	
	  class getResponseLogin extends AsyncTask<String, Integer, JSONObject> {
		  UserFunctions userFunction = new UserFunctions();
		  
	     protected JSONObject doInBackground(String... params) {
	    	
	    	 String email = params[0];
	    	 String password = params[1];
	    	 JSONObject json =  userFunction.loginUser(email, password);
	    	 
			return json;
				       
	     }

	     protected void onPostExecute(JSONObject json) {
             // check to make sure we're dealing with a string
	    	 try {
                 if (json.getString(KEY_SUCCESS) != null) {
                     loginErrorMsg.setText("");
                     String res = json.getString(KEY_SUCCESS); 
                     if(Integer.parseInt(res) == 1){
                         // user successfully logged in
                         // Store user details in SQLite Database
                         DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                         JSONObject json_user = json.getJSONObject("user");
                          
                         // Clear all previous data in database
                         userFunction.logoutUser(getApplicationContext());
                         db.addUser(json_user.getString(KEY_NAME), json_user.getString(KEY_EMAIL), json.getString(KEY_UID), json_user.getString(KEY_CREATED_AT));                        
                          
                         // Launch Dashboard Screen
                         
                         Intent dashboard = new Intent(getApplicationContext(), Dashboard.class);
                          
                         // Close all views before launching Dashboard
                         dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                         startActivity(dashboard);
                          
                         // Close Login Screen
                         finish();
                     }else{
                         // Error in login
                         loginErrorMsg.setText("Incorrect username/password");
                     }
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }
     }

		
	 }
	 
	
}
