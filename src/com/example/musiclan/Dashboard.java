package com.example.musiclan;

import java.io.File;
import java.net.URI;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.musiclan.Login.getResponseLogin;

import library.DatabaseHandler;
import library.UserFunctions;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.app.Activity;
import android.view.Menu;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.view.View;

public class Dashboard extends Activity {
	protected static final int REQ_CODE_PICK_SOUNDFILE = 11;
	UserFunctions userFunctions;
    ImageButton btnLogout;
    ImageButton btnContinue;
    ImageButton btnShare;
    ImageButton btnViewSongs;
    
    private static String KEY_SUCCESS = "success";
    private static String KEY_EMAIL = "email";
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_dashboard);
		/**
         * Dashboard Screen for the application
         * */       
        // Check login status in database
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_dashboard);
		 userFunctions = new UserFunctions();
	        if(userFunctions.isUserLoggedIn(getApplicationContext())){
	       // user already logged in show databoard
	            setContentView(R.layout.activity_dashboard);
	            btnLogout = (ImageButton) findViewById(R.id.btnLogout);
	            btnContinue = (ImageButton) findViewById(R.id.btnContinue);
	            btnShare = (ImageButton)findViewById(R.id.btn_share_songs);
	            btnViewSongs = (ImageButton)findViewById(R.id.btn_view_songs);
	            btnContinue.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						// TODO Auto-generated method stub
						/*Intent playMusic = new Intent(getApplicationContext(), PlayMusic.class);
						playMusic.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(playMusic);*/
						/*Intent wifiD = new Intent(getApplicationContext(), WiFiDirect.class);
						wifiD.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                    startActivity(wifiD);*/
						Intent search = new Intent(getApplicationContext(), Search.class);
						search.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                    startActivity(search);
						// Closing dashboard screen
	                   // finish();
					}
				});
	            
	            btnShare.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		                intent.setType("audio/*");
		                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_audio_file_title)), REQ_CODE_PICK_SOUNDFILE);
			
						
					}
				});
	            
	            
	            btnLogout.setOnClickListener(new View.OnClickListener() {
	                 
	                public void onClick(View arg0) {
	                    // TODO Auto-generated method stub
	                    userFunctions.logoutUser(getApplicationContext());
	                    Intent login = new Intent(getApplicationContext(), Login.class);
	                    login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                    startActivity(login);
	                    // Closing dashboard screen
	                    finish();
	                }
	            });
	             
	            btnViewSongs.setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						
						Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
						Uri uri = Uri.parse(Environment.getExternalStorageDirectory().getPath()
						    + "/MusicLan/");
						intent.setDataAndType(uri, "audio/*");
						startActivity(Intent.createChooser(intent, "Open folder"));
						
					}
				});
	            
	        }else{
	            // user is not logged in show login screen
	            Intent login = new Intent(getApplicationContext(), Login.class);
	            login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	            startActivity(login);
	            // Closing dashboard screen
	            finish();
	        }        
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == REQ_CODE_PICK_SOUNDFILE && resultCode == Activity.RESULT_OK){
	        if ((data != null) && (data.getData() != null)){
	            Uri audioFileUri = data.getData();
	            // Now you can use that Uri to get the file path, or upload it, ...
	            
	           // String songPath = getFilePathFromContentUri(audioFileUri,getApplicationContext().getContentResolver());
	            String[] songDetails = getFilePathFromContentUri(audioFileUri,getApplicationContext().getContentResolver());
	            String path = audioFileUri.getPath(); // "file:///mnt/sdcard/FileName.mp3"
	            //File file = new File(new URI(path));
	          
	           int id = Integer.parseInt(songDetails[0]);
	           String songName =  songDetails[1];
	           String artist =  songDetails[2];
	           String genre =  songDetails[3];
	           String songUrl =  songDetails[4];
	           
	             new getResponseSongShare().execute(songName, songUrl,genre,artist);
	            
	            
	            Toast.makeText(getApplicationContext(), "Shared "+ audioFileUri  + path, Toast.LENGTH_LONG).show();
	            }
	        }
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.dashboard, menu);
		return true;
	}

	  class getResponseSongShare extends AsyncTask<String, Integer, JSONObject> {
		  UserFunctions userFunction = new UserFunctions();
		  
	     protected JSONObject doInBackground(String... params) {
	    	
	    	 DatabaseHandler db = new DatabaseHandler(getApplicationContext());
	    	 HashMap<String,String> userDetails = db.getUserDetails();
	    	 
	    	 String email = userDetails.get("email");
	    	 String songName = params[0];
	    	 String songUrl = params[1];
	    	 String genre = params[2];
	    	 String artist = params[3];
	    	 
	    	 JSONObject json =  userFunction.shareSong(email, songName, songUrl, genre, artist);
	    	 
			return json;
				       
	     }

	     protected void onPostExecute(JSONObject json) {
             // check to make sure we're dealing with a string
	    	 try {
                 if (json.getString(KEY_SUCCESS) != null) {
                     //loginErrorMsg.setText("");
                     String res = json.getString(KEY_SUCCESS); 
                     if(Integer.parseInt(res) == 1){
                         // user successfully logged in
                         // Store user details in SQLite Database
                         DatabaseHandler db = new DatabaseHandler(getApplicationContext());
                         JSONObject json_user = json.getJSONObject("user");
                          
                         Toast.makeText(getApplicationContext(),json_user.getString("success") , Toast.LENGTH_SHORT).show();
                         
                         // Clear all previous data in database
                        // userFunction.logoutUser(getApplicationContext());
                        // db.addUser(json_user.getString(KEY_NAME), json_user.getString(KEY_EMAIL), json.getString(KEY_UID), json_user.getString(KEY_CREATED_AT));                        
                          
                         // Launch Dashboard Screen
                         
                        // Intent dashboard = new Intent(getApplicationContext(), Dashboard.class);
                          
                         // Close all views before launching Dashboard
                     //    dashboard.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                      //   startActivity(dashboard);
                          
                         // Close Login Screen
                    //     finish();
                     }else{
                         // Error in login
                       //  loginErrorMsg.setText("Incorrect username/password");
                     }
                 }
             } catch (JSONException e) {
                 e.printStackTrace();
             }
     }

		
	 }
	  
	  private String[] getFilePathFromContentUri(Uri selectedAudioUri, ContentResolver contentResolver) {
		   // String filePath;
		    String[] filePathColumn = {MediaStore.Audio.Media.DATA};
		
		    String[] songDetails = new String[5];
		    String[] proj = { MediaStore.Audio.Media._ID,
	                MediaStore.Audio.Media.DATA,
	                MediaStore.Audio.Media.TITLE,
	                MediaStore.Audio.Artists.ARTIST,
	                };
		    
		    String[] genreProj = {
		    		MediaStore.Audio.Genres.NAME,
		            MediaStore.Audio.Genres._ID
		    };
		    
		    Cursor cursor = contentResolver.query(selectedAudioUri, filePathColumn, null, null, null);
		    cursor.moveToFirst(); 
		    int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
		    songDetails[4] = cursor.getString(columnIndex);
		    
		    cursor = contentResolver.query(selectedAudioUri, proj, null, null, null);
		    cursor.moveToFirst();
		    //int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
		     columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID);
		    songDetails[0] = cursor.getString(columnIndex);
		    int musicId = Integer.parseInt(cursor.getString(columnIndex));
		    columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
		    songDetails[1] = cursor.getString(columnIndex);
		    columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST);
		    songDetails[2] = cursor.getString(columnIndex);
		    
		    Uri uri = MediaStore.Audio.Genres.getContentUriForAudioId("external", musicId);
		    cursor = contentResolver.query(uri, genreProj, null, null, null);
		    columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME);
		    cursor.moveToFirst(); 
//		    songDetails[3] = cursor.getString(columnIndex);
		 //   filePath = cursor.getString(columnIndex);
		    
		   
		    cursor.close();
		    return songDetails;
		}
}
