package com.example.musiclan;

import android.app.Activity;
import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.webkit.WebView.FindListener;
import android.widget.EditText;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;


/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class ChatService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_CHAT = "com.example.musiclan.SEND_CHAT";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public ChatService(String name) {
        super(name);
    }

    public ChatService() {
        super("ChatService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        //Activity activity = (Activity) context;
        EditText eText;
        if (intent.getAction().equals(ACTION_SEND_CHAT)) {
         //   String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            Socket socket = new Socket();
            try {
                Log.d(WiFiDirect.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirect.TAG, "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
               // ContentResolver cr = context.getContentResolver();
                
                PrintWriter out;
                out = new PrintWriter(stream);
                
             //   View mContentView = inflater.inflate(R.layout.peer_bottom_fragment, container, false);
                eText = (EditText) PeerBottomFragment.getPeerBottomFragmentActivity().findViewById(R.id.send_chat);        
                String msg = eText.getText().toString(); 
                if(msg!= null)
                {
                	out.println(msg);
                	Log.d("", msg);
				}
                
                
				out.flush();
                Log.d(WiFiDirect.TAG, "Client: Data written");
            } catch (IOException e) {
                Log.e(WiFiDirect.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }
            

        }
    }
}