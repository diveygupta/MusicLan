package com.example.musiclan;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.net.Uri;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.musiclan.DeviceListFragment.DeviceActionListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import library.DatabaseHandler;
import library.UserFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A fragment that manages a particular peer and allows interaction with device
 * i.e. setting up network connection and transferring data.
 */
public class DeviceDetailFragment extends Fragment implements ConnectionInfoListener {

	private static String KEY_SUCCESS = "success";
    protected static final int CHOOSE_FILE_RESULT_CODE = 20;
    protected static final int CHOOSE_TEXT_RESULT_CODE = 21;
    protected static final int CHOOSE_PEER_RESULT_CODE = 22;
    private static View mContentView = null;
    private WifiP2pDevice device;
    private WifiP2pInfo info;
    ProgressDialog progressDialog = null;
    private static ServerSocket serverSocket, songServerSocket;
    private static Socket client, songClient;
    private static InetAddress songRcvIp;
    private static ObjectOutputStream ostream, sostream;
    private static ObjectInputStream istream, sistream;
    private static Handler handler = new Handler();
    private static Handler handler2 = new Handler();
    private static String peerMacid;
    private static Thread songSendThread, chatThread, downloadSendThread;
    private SongSelection song = new SongSelection();
    static final String LOG_TAG = "UdpStream";
    String AUDIO_FILE_PATH = null;
    //static final String AUDIO_FILE_PATH = "/storage/sdcard0/Music/Avril Lavigne/The Best Damn Thing/Girlfriend.mp3";
    static final int AUDIO_PORT = 2048;
    static final int SAMPLE_RATE = 8000;
    static final int SAMPLE_INTERVAL = 480; // milliseconds
    static final int SAMPLE_SIZE = 2; // bytes per sample
    //static final int BUF_SIZE = SAMPLE_INTERVAL*SAMPLE_INTERVAL*SAMPLE_SIZE*2;
    static final int BUF_SIZE = 8000;
    boolean terminate = false;
    CountDownLatch latch = new CountDownLatch(1);
    CountDownLatch latchPause = new CountDownLatch(0);
    public static ArrayList<SongList> listOfSongs = new ArrayList<SongList>(); 
    
//    private Button mDoneButton;
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        mContentView = inflater.inflate(R.layout.device_detail, null);
  //      mDoneButton = (Button) mContentView.findViewById(R.id.btn_get_song_list); 
        
        mContentView.findViewById(R.id.btn_connect).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	if(listOfSongs.size() == 0)
            	{
            		Toast.makeText(getActivity(), "First press Get List button to retrieve list of songs", Toast.LENGTH_LONG).show();
            		return;
            	}
            	
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;
                config.wps.setup = WpsInfo.PBC;
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                progressDialog = ProgressDialog.show(getActivity(), "Press back to cancel",
                        "Connecting to :" + device.deviceAddress, true, true
//                        new DialogInterface.OnCancelListener() {
//
//                            @Override
//                            public void onCancel(DialogInterface dialog) {
//                                ((DeviceActionListener) getActivity()).cancelDisconnect();
//                            }
//                        }
                        );
                ((DeviceActionListener) getActivity()).connect(config);

            }
        });

        mContentView.findViewById(R.id.btn_disconnect).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	mContentView.findViewById(R.id.btn_peer_continue).setVisibility(View.GONE);
                        mContentView.findViewById(R.id.rcv_chat_server).setVisibility(View.GONE);
                        mContentView.findViewById(R.id.send_chat_server).setVisibility(View.GONE);
                        mContentView.findViewById(R.id.btn_send_chat_server).setVisibility(View.GONE);
                       /* try {
                        	if(client.isConnected())
                        		client.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}*/
                        ((DeviceActionListener) getActivity()).disconnect();
                    }
                });

        mContentView.findViewById(R.id.btn_start_client).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("audio/*");
                        startActivityForResult(intent, CHOOSE_FILE_RESULT_CODE);
                    }
                });
        
        mContentView.findViewById(R.id.btn_peer_continue).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // Allow user to pick an image from Gallery or other
                        // registered apps
                        Intent intent = new Intent(getActivity().getApplicationContext(),PeerActivity.class);
                        intent.putExtra("go_host",info.groupOwnerAddress.getHostAddress());
                        intent.putExtra("go_port", 8988);
                        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        //getActivity().finish();
                       // startActivityForResult(intent, CHOOSE_PEER_RESULT_CODE);
                    }
                });

        //send chat to peer
        mContentView.findViewById(R.id.btn_send_chat_server).setOnClickListener(
        new View.OnClickListener() {

            @Override
            public void onClick(View v) {
            	
                 EditText eText = (EditText) mContentView.findViewById(R.id.send_chat_server);        
                 String msg = eText.getText().toString(); 
                 if(msg != null || msg.length() > 0)
                 {
                 	
                	 try {
						ostream.writeObject(msg);
						ostream.flush();
						// ostream.close();
					} catch (IOException e) {	
						e.printStackTrace();
					}
                 	Log.d("", msg);
    			 }
                 eText.setText("");
                 Log.d(WiFiDirect.TAG, "Server: Data written");
                
            }
        });
        
        //get song list of peer
        mContentView.findViewById(R.id.btn_get_song_list).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                    	
                    	StringBuilder tempAddr = new StringBuilder(peerMacid);
                    	tempAddr.setCharAt(0,'0');
                    	tempAddr.setCharAt(1,'0');
                    	String macAddr = tempAddr.toString();
                    	macAddr = macAddr.toLowerCase();
                    	
                    	new getResponseSongsList(getActivity()).execute(macAddr);                       
                           Log.d(WiFiDirect.TAG, "Server: getting song list");
                        
                    }
                });
        
        songSendThread = new Thread(new Runnable(){

    		@Override
    		public void run() {
    			 Log.e(LOG_TAG, "start send thread, thread id: "
    	                    + Thread.currentThread().getId());
    	                long file_size = 0;
    	                int bytes_read = 0;
    	                int bytes_count = 0;
    	                String currentPath = null;
    	                try {
							latch.await();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
    	                File audio = new File(song.songPath);
    	                currentPath = song.songPath;
    	                FileInputStream audio_stream = null;
    	                file_size = audio.length();
    	                
    	              //  int bufsize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
    	                byte[] buf = new byte[BUF_SIZE];
    	                try
    	                {
    	                   // InetAddress addr = InetAddress.getLocalHost();
    	                	/*Socket socket = new Socket();
    	               	    ObjectOutputStream ostream;
    	               	    socket.bind(null);
    		                socket.connect((new InetSocketAddress("192.168.1.66", AUDIO_PORT)));
    		             
    		                ostream = new ObjectOutputStream(socket.getOutputStream());
    		                ostream.writeObject(file_size);
    		                socket.close();*/
    	                	 InetAddress addr = InetAddress.getByName(songRcvIp.getHostAddress());
    	                   
    	                	 DatagramSocket sock = new DatagramSocket();
    	                    audio_stream = new FileInputStream(audio);
    	                    
    	                   
    	                    while(bytes_count < file_size)
    	                    {
    	                    	
    	                        bytes_read = audio_stream.read(buf, 0, BUF_SIZE);
    	                        DatagramPacket pack = new DatagramPacket(buf, bytes_read,addr, AUDIO_PORT);
    	                        sock.send(pack);
    	                        bytes_count += bytes_read;
    	                        Log.d(LOG_TAG, "bytes_count : " + bytes_count);
    	                       
    	                        Thread.sleep(SAMPLE_INTERVAL, 0);
    	                        if(song.setPause == true)
    	                        {
    	                        	latchPause = new CountDownLatch(1);
    	                        	latchPause.await();
    	                        	//song.songPath = currentPath;
    	                        }
    	                        if(currentPath.compareTo(song.songPath) !=0 || bytes_count >= file_size){
    	                        	 if(bytes_count >= file_size){
    	    	                        	latch = new CountDownLatch(1);
    	    	                        	latch.await();
    	    	                        }
    	                        	audio = new File(song.songPath);
    	                        	currentPath = song.songPath;
    	                        	audio_stream = null;
    	        	                file_size = audio.length();
    	        	                audio_stream = new FileInputStream(audio);
    	        	                bytes_count = 0;
    	                    	}
    	                       
    	                    }
    	                  //  terminate = false;
    	                }
    	                catch (InterruptedException ie)
    	                {
    	                    Log.e(LOG_TAG, "InterruptedException");
    	                }
    	                catch (FileNotFoundException fnfe)
    	                {
    	                    Log.e(LOG_TAG, "FileNotFoundException");
    	                }
    	                catch (SocketException se)
    	                {
    	                    Log.e(LOG_TAG, "SocketException");
    	                }
    	                catch (UnknownHostException uhe)
    	                {
    	                    Log.e(LOG_TAG, "UnknownHostException");
    	                }
    	                catch (IOException ie)
    	                {
    	                    Log.e(LOG_TAG, "IOException");
    	                }
    	                
    	                
    		}
    		   
    	   });
        
        chatThread = new Thread(new Runnable(){

    		@Override
    		public void run() {
    			
    			try {
    				
   			//	Toast.makeText(, "Opening a chat server socket", Toast.LENGTH_SHORT).show();
   	            mContentView.findViewById(R.id.btn_peer_continue).setVisibility(View.GONE);
				serverSocket = new ServerSocket(8988);
				
                Log.d(WiFiDirect.TAG, "Server: Chat Socket opened");
                client = serverSocket.accept();
                Log.d(WiFiDirect.TAG, "Server: connection done");
                enableViews();
                istream = new ObjectInputStream(client.getInputStream());
                ostream = new ObjectOutputStream(client.getOutputStream());
                
                String msg = null;
                while(true)
                {
                msg = null;
                msg = (String) istream.readObject();
                displayMsg(msg);
                }
    			} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}    
    		}
    		   
    	   });
        
        
        downloadSendThread = new Thread(new Runnable(){

    		@Override
    		public void run() {
    			
    				Context context = getActivity();
    				String fileUri = song.songPath;
    	            String dhost = songRcvIp.getHostAddress();
    	            Socket dsocket = new Socket();
    	            int dport = 2050;

    	            try {
    	                //Log.d(WiFiDirectActivity.TAG, "Opening client socket - ");
    	                dsocket.bind(null);
    	                dsocket.connect((new InetSocketAddress(dhost, dport)));

    	                //Log.d(WiFiDirectActivity.TAG, "Client socket - " + socket.isConnected());
    	                OutputStream dstream = dsocket.getOutputStream();
    	                ContentResolver cr = context.getContentResolver();
    	                File audio = new File(fileUri);
    	                FileInputStream is = null;
    	                try {
    	                    is = new FileInputStream(audio);
    	                } catch (FileNotFoundException e) {
    	                    //Log.d(WiFiDirectActivity.TAG, e.toString());
    	                }
    	                copyFile(is, dstream);
    	                //Log.d(WiFiDirectActivity.TAG, "Client: Data written");
    	            } catch (IOException e) {
    	                //Log.e(WiFiDirectActivity.TAG, e.getMessage());
    	            } finally {
    	                if (dsocket != null) {
    	                    if (dsocket.isConnected()) {
    	                        try {
    	                            dsocket.close();
    	                        } catch (IOException e) {
    	                            // Give up
    	                            e.printStackTrace();
    	                        }
    	                    }
    	                }
    	            }
    			
    			
    		}
    		   
    	   });
        
        
      /*  songSendThread.start();
    	chatThread.start();*/

        return mContentView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        // User has picked an image. Transfer it to group owner i.e peer using
        // FileTransferService.
        Uri uri = data.getData();
        TextView statusText = (TextView) mContentView.findViewById(R.id.status_text);
        statusText.setText("Sending: " + uri);
        Log.d(WiFiDirect.TAG, "Intent----------- " + uri);
        Intent serviceIntent = new Intent(getActivity(), FileTransferService.class);
        serviceIntent.setAction(FileTransferService.ACTION_SEND_FILE);
        serviceIntent.putExtra(FileTransferService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(FileTransferService.EXTRAS_GROUP_OWNER_PORT, 8988);
        getActivity().startService(serviceIntent);
        
     /*   Intent serviceIntent = new Intent(getActivity(), ChatService.class);
        serviceIntent.setAction(ChatService.ACTION_SEND_CHAT);
        serviceIntent.putExtra(ChatService.EXTRAS_FILE_PATH, uri.toString());
        serviceIntent.putExtra(ChatService.EXTRAS_GROUP_OWNER_ADDRESS,
                info.groupOwnerAddress.getHostAddress());
        serviceIntent.putExtra(ChatService.EXTRAS_GROUP_OWNER_PORT, 8988);
        getActivity().startService(serviceIntent);*/
    }

    @Override
    public void onConnectionInfoAvailable(final WifiP2pInfo info) {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        this.info = info;
        this.getView().setVisibility(View.VISIBLE);

        // The owner IP is now known.
        TextView view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(getResources().getString(R.string.group_owner_text)
                + ((info.isGroupOwner == true) ? getResources().getString(R.string.yes)
                        : getResources().getString(R.string.no)));

        // InetAddress from WifiP2pInfo struct.
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText("Group Owner IP - " + info.groupOwnerAddress.getHostAddress());

        // After the group negotiation, we assign the group owner as the file
        // server. The file server is single threaded, single connection server
        // socket.
        if (info.groupFormed && info.isGroupOwner) {
        	mContentView.findViewById(R.id.btn_get_song_list).setVisibility(View.GONE);
        	if(songSendThread == null || !songSendThread.isAlive())
        		songSendThread.start();
        	if(chatThread == null || !chatThread.isAlive())
          	chatThread.start();
          	//new FileServerAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text),(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).execute();
        	new SongAsyncTask(getActivity(), mContentView.findViewById(R.id.status_text),(LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).execute();
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case, we enable the
            // get file button.
        //    mContentView.findViewById(R.id.btn_start_client).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.btn_peer_continue).setVisibility(View.VISIBLE);
            mContentView.findViewById(R.id.btn_get_song_list).setVisibility(View.VISIBLE);
            ((TextView) mContentView.findViewById(R.id.status_text)).setText(getResources().getString(R.string.client_text));
        }
        
        // hide the connect button
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.GONE);
        
        //start the peer activity
        /*Intent intent = new Intent(getActivity().getApplicationContext(),PeerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);*/
    }

    /**
     * Updates the UI with device data
     * 
     * @param device the device to be displayed
     */
    public void showDetails(WifiP2pDevice device) {
        this.device = device;
        this.getView().setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(device.deviceAddress);
        peerMacid = device.deviceAddress;
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(device.toString());

    }

    /**
     * Clears the UI fields after a disconnect or direct mode disable operation.
     */
    public void resetViews() {
        mContentView.findViewById(R.id.btn_connect).setVisibility(View.VISIBLE);
        TextView view = (TextView) mContentView.findViewById(R.id.device_address);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.device_info);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.group_owner);
        view.setText(R.string.empty);
        view = (TextView) mContentView.findViewById(R.id.status_text);
        view.setText(R.string.empty);
        mContentView.findViewById(R.id.btn_start_client).setVisibility(View.GONE);
        this.getView().setVisibility(View.GONE);
    }

 
    
    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
   /* public static class FileServerAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        private LayoutInflater inflater;
        
        *//**
         * @param context
         * @param statusText
         *//*
        public FileServerAsyncTask(Context context, View statusText,LayoutInflater inflater) {
            this.context = context;
            this.statusText = (TextView) statusText;
            this.inflater = inflater;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
            	serverSocket = new ServerSocket(8988);
                Log.d(WiFiDirect.TAG, "Server: Chat Socket opened");
                client = serverSocket.accept();
                Log.d(WiFiDirect.TAG, "Server: connection done");
                enableViews();
                istream = new ObjectInputStream(client.getInputStream());
                ostream = new ObjectOutputStream(client.getOutputStream());
                
                String msg = null;
                while(true)
                {
                msg = null;
                msg = (String) istream.readObject();
                displayMsg(msg);
                }
               // serverSocket.close();
               // return msg;
            } catch (Exception e) {
                Log.e(WiFiDirect.TAG, e.getMessage());
                return null;
            } 
        }

        
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                context.startActivity(intent);
            	
            	 
    			// View mView = inflater.inflate(R.layout.peer_bottom_fragment, null);
    			 TextView tview = (TextView) mContentView.findViewById(R.id.rcv_chat_server);
    			 tview.setText(result);
            }

        }

        
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         
        @Override
        protected void onPreExecute() {
         //   statusText.setText("Opening a server socket");
        	Toast.makeText(context, "Opening a chat server socket", Toast.LENGTH_SHORT).show();
            mContentView.findViewById(R.id.btn_peer_continue).setVisibility(View.GONE);
            
        }

       // 
        
    }*/
    
    public static void displayMsg(final String msg)
    {
 	   
 	   handler.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				 TextView tview = (TextView) mContentView.findViewById(R.id.rcv_chat_server);
    			 tview.setText(msg);
 			}
 		});
       
 	   
    }
	  
    public static void enableViews()
    {
 	   
 	   handler.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				mContentView.findViewById(R.id.rcv_chat_server).setVisibility(View.VISIBLE);
 	            mContentView.findViewById(R.id.send_chat_server).setVisibility(View.VISIBLE);
 	            mContentView.findViewById(R.id.btn_send_chat_server).setVisibility(View.VISIBLE);
 			}
 		});
       
 	   
    }
    public static boolean copyFile(FileInputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);

            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d(WiFiDirect.TAG, e.toString());
            return false;
        }
        return true;
    }
    
    /**
     * A simple server socket that accepts connection and writes some data on
     * the stream.
     */
    public class SongAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private TextView statusText;
        private LayoutInflater inflater;
        
        /**
         * @param context
         * @param statusText
         */
        public SongAsyncTask(Context context, View statusText,LayoutInflater inflater) {
            this.context = context;
            this.statusText = (TextView) statusText;
            this.inflater = inflater;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
            	songServerSocket = new ServerSocket(8989);
                Log.d(WiFiDirect.TAG, "Server: Socket opened");
                songClient = songServerSocket.accept();
                songRcvIp = songClient.getInetAddress();
                Log.d(WiFiDirect.TAG, "Server: connection done");
                //enableViews();
                sistream = new ObjectInputStream(songClient.getInputStream());
                sostream = new ObjectOutputStream(songClient.getOutputStream());
                
                while(true)
                {
                	song = (SongSelection) sistream.readObject();                	
                	//AUDIO_FILE_PATH = song.songPath;
                	if(song.setDownload && !song.setPlay)
                	{
                		downloadSendThread.start();
                		continue;
                	}
                	latch.countDown();
                	if(!song.setPause)
                	{
                		latchPause.countDown();
                	}
               // runSongThread();
                }
                
                
                /*String msg = null;
                while(true)
                {
                msg = null;
                msg = (String) sistream.readObject();
                displayMsg(msg);
                }*/
               // serverSocket.close();
               // return null;
            } catch (Exception e) {
                //Log.e(WiFiDirect.TAG, e.getMessage());
                return null;
            } 
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
         */
        @Override
        protected void onPostExecute(String result) {
          /*  if (result != null) {
                statusText.setText("File copied - " + result);
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + result), "image/*");
                context.startActivity(intent);
            	
            	 
    			// View mView = inflater.inflate(R.layout.peer_bottom_fragment, null);
    			 TextView tview = (TextView) mContentView.findViewById(R.id.rcv_chat_server);
    			 tview.setText(result);
            }*/
        	Toast.makeText(context, "songPath" + song.songPath, Toast.LENGTH_LONG).show();
        	//songSendThread.start();
        }

        /*
         * (non-Javadoc)
         * @see android.os.AsyncTask#onPreExecute()
         */
        @Override
        protected void onPreExecute() {
           // statusText.setText("Opening a server socket");
        	Toast.makeText(context, "Opening a song server socket", Toast.LENGTH_SHORT).show();
            mContentView.findViewById(R.id.btn_peer_continue).setVisibility(View.GONE);
            
        }

       // 
        
    }
    
/*    public void runSongThread()
    {
 	   
 	   handler2.post(new Runnable() {
 			
 			@Override
 			public void run() {
 				if(songSendThread.isAlive() && songSendThread.getState() != Thread.State.TERMINATED)
 				{
 					terminate = true;
 					try {
						songSendThread.join();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
 				}
 				
 					songSendThread.start();
 			}
 		});
       
 	   
    }*/
    
    class getResponseSongsList extends AsyncTask<String, Integer, JSONObject>{
		  
    	Context context;
    		UserFunctions userFunction = new UserFunctions();
		  
		  public getResponseSongsList(Context context) {
			// TODO Auto-generated constructor stub
			  this.context = context;
		}
		  
		     protected JSONObject doInBackground(String... params) {
		    	
		    	 String macAddr = params[0];		    	 
		    	 JSONObject json = userFunction.getSongsList(macAddr);
		    	 
				return json;    
		     }

		     protected void onPostExecute(JSONObject json) {
		    	 try {
	                    if (json.getString(KEY_SUCCESS) != null) {
	                       
	                        String res = json.getString(KEY_SUCCESS); 
	                        if(Integer.parseInt(res) == 1){
	                         
	                     	  JSONArray list = json.getJSONArray("list");
	                     	  for(int i = 0;i<list.length();i++) {
	                     		  JSONObject obj = list.getJSONObject(i);
	                     		 SongList sl = new SongList();
	                     		 sl.song = obj.getString("song");
	                     		 sl.song_path = obj.getString("song_path");
	                     		 sl.artist = obj.getString("artist");
	                     		 listOfSongs.add(sl);
	                     	  }
	                     	  
	                     	  //display as a list
	                     	 final CharSequence[] items = new CharSequence[listOfSongs.size()];
	                      	for(int i=0;i<listOfSongs.size();i++){
	                          	SongList s = listOfSongs.get(i); 
	                          	items[i] = s.song;
	                          }
	                      	
	                      		if(listOfSongs.size()>0){
	                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
	                      			Toast.makeText(context, "User hasn't shared songs", Toast.LENGTH_SHORT).show();
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
		    	 listOfSongs.clear();
		     }
		     
		     
		    
}
    
    
	   
}

