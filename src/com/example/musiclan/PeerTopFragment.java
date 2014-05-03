package com.example.musiclan;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import com.example.musiclan.R;
import com.example.musiclan.R.layout;

import android.app.Fragment;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class PeerTopFragment extends Fragment{
	
	private View mContentView = null;
	private Button btnReqList,btnPause; 
	private Thread socketThread, songRcvThread;
	private Socket socket = new Socket();
	private ObjectOutputStream sostream;
	private ObjectInputStream sistream;
	private String host;
	ListView listView ;
	Context context;
	String songPath = null;
	boolean isSockOpen = false, setPause = false;
	public String currentPlayingSong = null;
	static final String LOG_TAG = "UdpStream";
    //static final String AUDIO_FILE_PATH = "/storage/sdcard0/Music/tu.wav";
    //static final String AUDIO_FILE_PATH = "/storage/sdcard0/Music/Avril Lavigne/The Best Damn Thing/Girlfriend.mp3";
    static final int AUDIO_PORT = 2048;
    static final int SAMPLE_RATE = 8000;
    static final int SAMPLE_INTERVAL = 480; // milliseconds
    static final int SAMPLE_SIZE = 2; // bytes per sample
    //static final int BUF_SIZE = SAMPLE_INTERVAL*SAMPLE_INTERVAL*SAMPLE_SIZE*2;
    
    static final int BUF_SIZE = 8000;
	@Override
   public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
      /**
       * Inflate the layout for this fragment
       */
    //  return inflater.inflate(R.layout.peer_top_fragment, container, false);
      context = getActivity();
      mContentView = inflater.inflate(R.layout.peer_top_fragment, container, false);
    
      listView = (ListView)mContentView.findViewById(R.id.list);
      
      final ArrayList<SongList> listOfSongs = DeviceDetailFragment.listOfSongs;
      PeerActivity activity = (PeerActivity) getActivity();
	  host = activity.getHost();
      
      btnReqList = (Button) mContentView.findViewById(R.id.btn_Req_List);
      btnReqList.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			  // Get ListView object from xml          
            // Defined Array values to show in ListView
            String[] values = new String[listOfSongs.size()];
            for(int i=0;i<listOfSongs.size();i++){
            	SongList s = listOfSongs.get(i); 
            	values[i] = s.song;
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,android.R.layout.simple_list_item_1, android.R.id.text1, values);
                    
                  // Assign adapter to ListView
            listView.setAdapter(adapter); 
			
		}
	});
      
      btnPause = (Button) mContentView.findViewById(R.id.btn_Pause_Song);
      btnPause.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(isSockOpen){
           	 try {
				if(setPause){
					SongSelection song = new SongSelection();
	    		 	song.setPause = false;
	    		 	song.setPlay = true;
					song.songPath = currentPlayingSong;
					setPause = false;
					sostream.writeObject(song);
					sostream.flush();
				} else{
					SongSelection song = new SongSelection();
	    		 	song.setPause = true;
	    		 	song.setPlay = false;
	    		 	song.songPath = currentPlayingSong;
				    setPause = true;
				    sostream.writeObject(song);
					sostream.flush();
				}
		}
           	catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	});
      
     // songRcvThread.start();
      
      // ListView Item Click Listener
      listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
              
             // ListView Clicked item index
             int itemPosition = position;
             
             // ListView Clicked item value
             String  itemValue = (String) listView.getItemAtPosition(position);
             for(SongList s:listOfSongs){
            	 if(s.song.compareTo(itemValue) == 0)
            		 songPath = s.song_path;	 
             }            
              
              // Show Alert 
             Toast.makeText(context,"Position :"+itemPosition+"  Song : " + itemValue +"song path: "+ songPath, Toast.LENGTH_LONG).show();
           
             if(isSockOpen){
            	 try {
            		 	SongSelection song = new SongSelection();
            		 	song.setPause = false;
            		 	song.setPlay = true;
            		 	song.songPath = songPath;
            		 	currentPlayingSong = songPath;
						sostream.writeObject(song);
						sostream.flush();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
             }
             else
            	 socketThread.start();
            	 
           if(songRcvThread == null || !songRcvThread.isAlive())  
             songRcvThread.start();
            
            }

			

       }); 
      
	   socketThread = new Thread(new Runnable(){
		    @Override
		    public void run() {
		        try {
		        	 Log.d(WiFiDirect.TAG, "Opening client socket - ");
		                socket.bind(null);
		                socket.connect((new InetSocketAddress(host, 8989)));
		              //  InetAddress localIP = socket.getInetAddress();
		                sostream = new ObjectOutputStream(socket.getOutputStream());
		                sistream = new ObjectInputStream(socket.getInputStream());
		                if(songPath != null)
		                {
		                	SongSelection song = new SongSelection();
	            		 	song.setPause = false;
	            		 	song.setPlay = true;
	            		 	song.songPath = songPath;
	            		 	currentPlayingSong = songPath;
							sostream.writeObject(song);
		                	sostream.flush();
		                	isSockOpen = true;
		                }
						
		                Log.d(WiFiDirect.TAG, "Client socket - " + socket.isConnected());
		                
		        } catch (Exception e) {
		            e.printStackTrace();
		        }
		        finally {
	                if (socket != null) {
	                    if (socket.isConnected()) {
	                        try {
	                          //  socket.close();
	                        } catch (Exception e) {
	                            // Give up
	                            e.printStackTrace();
	                        }
	                    }
	                }
	            }
		    }
		});
      
	   songRcvThread = new Thread(new Runnable(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			  Log.e(LOG_TAG, "start recv thread, thread id: "
	                    + Thread.currentThread().getId());
	                
	              //  int bufsize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);
	                
	                AudioTrack track = new AudioTrack(AudioManager.STREAM_MUSIC,SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, 
	                        AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM);
	                
	              //  AudioTrack track = findAudioRecord();
	                track.play();
	                try
	                {
	                	/*ServerSocket serverSocket;
	            	    Socket client;
	            	    ObjectInputStream istream;
	            	    serverSocket = new ServerSocket(AUDIO_PORT);
	            	    client = serverSocket.accept();
	            	    istream = new ObjectInputStream(client.getInputStream());
	            	    Long bsize = (Long) istream.readObject();
	            	    serverSocket.close();*/
	                    
	            	    //byte[] totalBuf = new byte[bsize];
	                    
	            	//    byte[] buf = new byte[BUF_SIZE];
	            	    int i = 0;
	                    //ArrayList<byte[]> total = new ArrayList<byte[]>(); 
	            	    DatagramSocket sock = new DatagramSocket(AUDIO_PORT);
	                    while(true)
	                    {
	                    	byte[] buf = new byte[BUF_SIZE];
	                        DatagramPacket pack = new DatagramPacket(buf, BUF_SIZE);
	                        sock.receive(pack);
	                        Log.d(LOG_TAG, "recv pack: " + pack.getLength());
	                      //  total.add(pack.getData());
	                       // track.write(total.get(i), 0, total.get(i).length);
	                        track.write(pack.getData(),0,pack.getLength());
	                        i++;
	                        
	                      //  playMp3(pack.getData());
	                    }
	                }
	                catch (SocketException se)
	                {
	                    Log.e(LOG_TAG, "SocketException: " + se.toString());
	                }
	                catch (IOException ie)
	                {
	                    Log.e(LOG_TAG, "IOException" + ie.toString());
	                }
	                
	                
		}
		   
	   });
	   
      return mContentView;
   }
	
	
}