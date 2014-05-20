package com.example.musiclan;

import library.*;

import org.json.*;

import com.example.musiclan.Login.getResponseLogin;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.app.Activity;
import android.view.Menu;
import android.view.inputmethod.EditorInfo;

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
    private WifiP2pManager manager;
	private final IntentFilter intentFilter = new IntentFilter();
	private Channel channel;
	private BroadcastReceiver receiver = null;
    static String deviceName;
    
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
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
		 manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
	     channel = manager.initialize(this, getMainLooper(), null);
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
               
               // JSONObject json = userFunction.registerUser(name, email, password);
            	registerUser();
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
        
        inputPassword.setOnEditorActionListener(new OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                	registerUser();
                }    
                return false;
            }
        });
        
    }
	
	void registerUser(){
		 String name = inputFullName.getText().toString();
         String email = inputEmail.getText().toString();
         String password = inputPassword.getText().toString();
         
         if(name.length() == 0 || email.length() == 0 || password.length() == 0){
         	Toast.makeText(getApplicationContext(),"Field missing", Toast.LENGTH_SHORT).show();
         	return;
         }
         
         if(!email.contains("@") || !email.contains(".com")){
         	Toast.makeText(getApplicationContext(),"Incorrect email id", Toast.LENGTH_SHORT).show();
         	return;
         }
         if(password.length() < 6){
         	Toast.makeText(getApplicationContext(),"Enter password of Min length 6", Toast.LENGTH_SHORT).show();
         	return;
         }
         //UserFunctions userFunction = new UserFunctions();
         new getResponseRegsiter().execute(name,email,password,macAddr,deviceName);
	}
	
	
	 /** register the BroadcastReceiver with the intent values to be matched */
    @Override
    public void onResume() {
        super.onResume();
        receiver = new Broadcast(manager, channel, this);
        registerReceiver(receiver, intentFilter);
    }
    
    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
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
		    	 String deviceName = params[4];
		    	 JSONObject json = userFunction.registerUser(name, email, password,macAddr,deviceName);
		    	 
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
