package com.example.musiclan;

import library.*;

import org.json.*;

import com.example.musiclan.Login.getResponseLogin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.app.Activity;
import android.view.Menu;

public class Register extends Activity {
	Button btnRegister;
    Button btnLinkToLogin;
    EditText inputFullName;
    EditText inputEmail;
    EditText inputPassword;
    TextView registerErrorMsg;
    String macAddr;
    
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
		setContentView(R.layout.activity_register);
		 // Importing all assets like buttons, text fields
        inputFullName = (EditText) findViewById(R.id.registerName);
        inputEmail = (EditText) findViewById(R.id.registerEmail);
        inputPassword = (EditText) findViewById(R.id.registerPassword);
        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLinkToLogin = (Button) findViewById(R.id.btnLinkToLoginScreen);
        registerErrorMsg = (TextView) findViewById(R.id.register_error);
       
        WifiManager wifiMan = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        	WifiInfo wifiInf = wifiMan.getConnectionInfo();
        	macAddr = wifiInf.getMacAddress(); 
        	StringBuilder tempAddr = new StringBuilder(macAddr);
        	tempAddr.setCharAt(0,'0');
        	tempAddr.setCharAt(1,'0');
            macAddr = tempAddr.toString();
            macAddr = macAddr.toLowerCase();
        	// Register Button Click event
        btnRegister.setOnClickListener(new View.OnClickListener() {         
            public void onClick(View view) {
                String name = inputFullName.getText().toString();
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                //UserFunctions userFunction = new UserFunctions();
                new getResponseRegsiter().execute(name,email,password,macAddr);
               // JSONObject json = userFunction.registerUser(name, email, password);
                 
                // check for login response
                
            }
        });
 
        // Link to Login Screen
        btnLinkToLogin.setOnClickListener(new View.OnClickListener() {
 
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        Login.class);
                startActivity(i);
                // Close Registration View
                finish();
            }
        });
    }
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sign_up, menu);
		return true;
	}

	
	class getResponseRegsiter extends AsyncTask<String, Integer, JSONObject>{
		  UserFunctions userFunction = new UserFunctions();
		  
		     protected JSONObject doInBackground(String... params) {
		    	
		    	 String name = params[0];
		    	 String email = params[1];
		    	 String password = params[2];
		    	 String macAddr = params[3];
		    	 JSONObject json = userFunction.registerUser(name, email, password,macAddr);
		    	 
				return json;    
		     }

		     protected void onPostExecute(JSONObject json) {
		    	 try {
	                    if (json.getString(KEY_SUCCESS) != null) {
	                        registerErrorMsg.setText("");
	                        String res = json.getString(KEY_SUCCESS); 
	                        if(Integer.parseInt(res) == 1){
	                            // user successfully registred
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
	                            // Close Registration Screen
	                            finish();
	                        }else{
	                            // Error in registration
	                            registerErrorMsg.setText("Error occured in registration");
	                        }
	                    }
	                } catch (JSONException e) {
	                    e.printStackTrace();
	                } 
		
	}
}
}
