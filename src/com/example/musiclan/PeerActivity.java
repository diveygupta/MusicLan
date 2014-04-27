package com.example.musiclan;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;

public class PeerActivity extends Activity {
	
	 public static final String ACTION_PEER = "com.example.musiclan.PEER";
	 String host = null;
	 int port;
	 
	 public String getHost() {
	        return host;
	    }
	 public int getPort() {
	        return port;
	    }
	 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		 host = getIntent().getExtras().getString("go_host");
		 port = getIntent().getExtras().getInt("go_port");
		
		setContentView(R.layout.activity_peer);
		
		
		
		/*	Bundle bundle = new Bundle();
		bundle.putString("edttext", "From Activity");
		// set Fragmentclass Arguments
		PeerBottomFragment fragobj = new PeerBottomFragment();
		fragobj.setArguments(bundle);*/
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.peer, menu);
		return true;
	}

}
