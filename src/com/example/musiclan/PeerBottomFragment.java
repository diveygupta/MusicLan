package com.example.musiclan;


import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.example.musiclan.R;
import com.example.musiclan.R.layout;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class PeerBottomFragment extends Fragment{
	 private static final int SOCKET_TIMEOUT = 5000;
	 private View mContentView = null;
	 private Button btnSendChat; 
	 private WifiP2pInfo info;
	 public static Activity PeerBottomFragmentActivity;
	 private Button btnStartChat;
	 private String host;
	 private int port;
	 private Thread socketThread,rcvThread;
	 private static Socket socket = new Socket();
	 private ObjectOutputStream ostream;
	 private ObjectInputStream istream;
     private Handler handler = new Handler();

	 public static Activity getPeerBottomFragmentActivity()
	 {
		 return PeerBottomFragmentActivity;
	 }
	 
   @Override
   public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
      /**
       * Inflate the layout for this fragment
       */
	   mContentView = inflater.inflate(R.layout.peer_bottom_fragment, container, false);
	   btnSendChat = (Button) mContentView.findViewById(R.id.btn_send);
	   btnStartChat = (Button) mContentView.findViewById(R.id.btn_Start_Chat);
	   
	   PeerBottomFragmentActivity = getActivity();
	     
	   PeerActivity activity = (PeerActivity) getActivity();
	   host  = activity.getHost();
	   port  = activity.getPort();
	   
	   btnStartChat.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {

			 mContentView.findViewById(R.id.rcv_chat).setVisibility(View.VISIBLE);
			 mContentView.findViewById(R.id.send_chat).setVisibility(View.VISIBLE);
			 mContentView.findViewById(R.id.btn_send).setVisibility(View.VISIBLE);
			
			 socketThread.start();
			// rcvThread.start();
		}
	});
	   
	   
	   socketThread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		        try {
		        	 Log.d(WiFiDirect.TAG, "Opening client socket - ");
		                socket.bind(null);
		                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
		                ostream = new ObjectOutputStream(socket.getOutputStream());
		                istream = new ObjectInputStream(socket.getInputStream());
		                Log.d(WiFiDirect.TAG, "Client socket - " + socket.isConnected());
		                rcvThread.start();
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        finally {
	                if (socket != null) {
	                    if (socket.isConnected()) {
	                        try {
	                         //   socket.close();
	                        } catch (Exception e) {
	                            // Give up
	                            e.printStackTrace();
	                        }
	                    }
	                }
	            }
		    }
		});
	   
	   
	   rcvThread = new Thread(new Runnable(){
		@Override
		public void run() {
			
			while(true)
				{
				String msg = null;
	            try {
	            		if(istream == null)
	            			istream = new ObjectInputStream(socket.getInputStream());
						msg = (String) istream.readObject();
						//istream.close();
				} catch (IOException e) {					
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
				
	           displayMsg(msg);
			}
	          
		}		    
	   });
	   
	   
	   btnSendChat.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
/*
		        Log.d(WiFiDirect.TAG, "Intent-----------chat ");
		        Intent intent = new Intent(getActivity(), ChatService.class);
		        intent.setAction(ChatService.ACTION_SEND_CHAT);
		        intent.putExtra(ChatService.EXTRAS_GROUP_OWNER_ADDRESS,host);
		        intent.putExtra(ChatService.EXTRAS_GROUP_OWNER_PORT, port);
		        getActivity().startService(intent);*/
			
		//	 PrintWriter out;
          //   out = new PrintWriter(ostream);
             EditText eText = (EditText) getActivity().findViewById(R.id.send_chat);        
             String msg = eText.getText().toString(); 
             if(msg!= null)
             {
             	//out.println(msg);
            	 try {
					ostream.writeObject(msg);
					ostream.flush();
            	 } catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	 Log.d("", msg);
			 }
             
			// out.flush();
             Log.d(WiFiDirect.TAG, "Client: Data written");
             eText.setText("");
		}
	} );
	   
	   
	   return mContentView;
   }
   
   public void displayMsg(final String msg)
   {
	   
	   handler.post(new Runnable() {
			
			@Override
			public void run() {
				 TextView tText = (TextView) getActivity().findViewById(R.id.rcv_chat);
			       tText.setBackgroundColor(12);			     
			       tText.setText(msg);
			}
		});
      
	   
   }
}