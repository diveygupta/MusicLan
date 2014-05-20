package com.example.musiclan;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import library.UserFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.musiclan.DeviceDetailFragment.getResponseSongsList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class Search extends Activity {

	ImageButton btnSearchPeer;
	Button btnSearchSong,btnSearchArtist;
	EditText editSongName,editArtistName;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_search);
		
		btnSearchPeer = (ImageButton) findViewById(R.id.btn_search_peer);
		btnSearchSong = (Button) findViewById(R.id.btn_search_song);
		editSongName = (EditText) findViewById(R.id.edit_search_song);
		btnSearchArtist = (Button) findViewById(R.id.btn_search_artist);
		editArtistName = (EditText) findViewById(R.id.edit_search_artist);
		
		btnSearchPeer.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
				
					Intent wifiD = new Intent(getApplicationContext(), WiFiDirect.class);
					wifiD.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   startActivity(wifiD);
					
					// Closing dashboard screen
                  // finish();
				}
			});
		
		btnSearchSong.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			
				String songName = editSongName.getText().toString();
				new getResponseSearchSong(getApplicationContext()).execute(songName); 
			}
		});
		
		btnSearchArtist.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
			
				String artistName = editArtistName.getText().toString();
				new getResponseSearchArtist(getApplicationContext()).execute(artistName); 
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.search, menu);
		return true;
	}
	
	
    class getResponseSearchSong extends AsyncTask<String, Integer, JSONObject>{
		  
    	private static final String KEY_SUCCESS = "success";
		Context context;
    		UserFunctions userFunction = new UserFunctions();
		  
		  public getResponseSearchSong(Context context) {
			// TODO Auto-generated constructor stub
			  this.context = context;
		}
		  
		     protected JSONObject doInBackground(String... params) {
		    	
		    	 String songName = params[0].toLowerCase();		    	 
		    	 JSONObject json = userFunction.getPeerList(songName);
		    	 
				return json;    
		     }

		     protected void onPostExecute(JSONObject json) {
		    	 try {
		    		 ArrayList<String> listOfPeers = new ArrayList<String>(); 
	                    if (json.getString(KEY_SUCCESS) != null) {
	                       
	                        String res = json.getString(KEY_SUCCESS); 
	                        if(Integer.parseInt(res) == 1){
	                         
	                     	  JSONArray list = json.getJSONArray("list");
	                     	  for(int i = 0;i<list.length();i++) {
	                     		  JSONObject obj = list.getJSONObject(i);
	                     		  String peerDeviceName = obj.getString("device_name");
	                     		  String peerName = obj.getString("name");
	                     		 String peerMac = obj.getString("macid");
	                     		  listOfPeers.add(peerName + " - " + peerDeviceName + "-" + peerMac);
	                     		 
	                     	  }
	                     	  
	                     	  //display as a list
	                     	 final CharSequence[] items = new CharSequence[listOfPeers.size()];
	                      	for(int i=0;i<listOfPeers.size();i++){
	                      		String peer = listOfPeers.get(i); 
	                          	items[i] = peer;
	                          }
	                      	
	                      		if(listOfPeers.size()>0){
	                            AlertDialog.Builder builder = new AlertDialog.Builder(Search.this);
	                            builder.setTitle("List of Peers");
	                            builder.setItems(items, new DialogInterface.OnClickListener() {
	                                public void onClick(DialogInterface dialog, int item) {
	                                    // Do something with the selection
	                            //        mDoneButton.setText(items[item]);
	                                }
	                            });
	                            
	                            AlertDialog alert = builder.create();
	                            alert.show();
	                      		}else{
	                      			Toast.makeText(context, "Users haven't shared this song", Toast.LENGTH_SHORT).show();
	                      		}
	                     	  
	                       
	                        }else{
	                            // Error in registration
	                       //     registerErrorMsg.setText("Error occured in registration");
	                        	Toast.makeText(context, "User doesn't exist", Toast.LENGTH_SHORT).show();
	                        }
	                    }
	                } catch (JSONException e) {
	                    e.printStackTrace();
	                } 
		
	}
		     @Override
		     protected void onPreExecute(){
		    	// listOfPeers.clear();
		     }
		     
		     
		    
}
    
    class getResponseSearchArtist extends AsyncTask<String, Integer, JSONObject>{
		  
    	private static final String KEY_SUCCESS = "success";
		Context context;
    		UserFunctions userFunction = new UserFunctions();
		  
		  public getResponseSearchArtist(Context context) {
			// TODO Auto-generated constructor stub
			  this.context = context;
		}
		  
		     protected JSONObject doInBackground(String... params) {
		    	
		    	 String artistName = params[0].toLowerCase();		    	 
		    	 JSONObject json = userFunction.getArtistList(artistName);
		    	 
				return json;    
		     }

		     protected void onPostExecute(JSONObject json) {
		    	 try { 
		    		 Set<String> listofsongs = new HashSet<String>(); 
	                    if (json.getString(KEY_SUCCESS) != null) {
	                       
	                        String res = json.getString(KEY_SUCCESS); 
	                        if(Integer.parseInt(res) == 1){
	                         
	                     	  JSONArray list = json.getJSONArray("list");
	                     	  for(int i = 0;i<list.length();i++) {
	                     		  JSONObject obj = list.getJSONObject(i);
	                     		  //String email = obj.getString("email");
	                     		  String songName = obj.getString("song");
	                     		 listofsongs.add(songName);
	                     		 
	                     	  }
	                     	  
	                     	  //display as a list
	                     	 final CharSequence[] items = new CharSequence[listofsongs.size()];
	                     	
	                     	 Iterator<String> iterator = listofsongs.iterator();
	                     	 int i = 0;
	                     	while(iterator.hasNext()){
	                     	  String peer = (String) iterator.next();
	                     		items[i] = peer;
	                     		i++;
	                     	}
	                      
	                      	
	                      		if(listofsongs.size()>0){
	                            AlertDialog.Builder builder = new AlertDialog.Builder(Search.this);
	                            builder.setTitle("List of Songs");
	                            builder.setItems(items, new DialogInterface.OnClickListener() {
	                                public void onClick(DialogInterface dialog, int item) {
	                                    // Do something with the selection
	                            //        mDoneButton.setText(items[item]);
	                                }
	                            });
	                            
	                            AlertDialog alert = builder.create();
	                            alert.show();
	                      		}else{
	                      			Toast.makeText(context, "No songs for this artists", Toast.LENGTH_SHORT).show();
	                      		}
	                     	  
	                       
	                        }else{
	                            // Error in registration
	                       //     registerErrorMsg.setText("Error occured in registration");
	                        	Toast.makeText(context, "User doesn't exist", Toast.LENGTH_SHORT).show();
	                        }
	                    }
	                } catch (JSONException e) {
	                    e.printStackTrace();
	                } 
		
	}
		     @Override
		     protected void onPreExecute(){
		    	// listofsongs.clear();
		     }
		     
		     
		    
}
}
