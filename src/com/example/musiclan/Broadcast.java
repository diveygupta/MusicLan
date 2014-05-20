package com.example.musiclan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

public class Broadcast extends BroadcastReceiver{

		private WifiP2pManager manager;
	    private Channel channel;
	    private Register activity;
	
	    /**
	     * @param manager WifiP2pManager system service
	     * @param channel Wifi p2p channel
	     * @param activity activity associated with the receiver
	     */
	    public Broadcast(WifiP2pManager manager, Channel channel,Register activity) {
	        super();
	        this.manager = manager;
	        this.channel = channel;
	        this.activity = activity;
	    }
	    
	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
           
			activity.deviceName = ((WifiP2pDevice) intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)).deviceName;
			
        }
	}

}
