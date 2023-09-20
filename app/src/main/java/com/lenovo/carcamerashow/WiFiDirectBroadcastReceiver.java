package com.lenovo.carcamerashow;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

/**
 * A BroadcastReceiver that notifies of important wifi p2p events.
 */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
    private WifiP2pManager manager;
    private Channel channel;
    private Activity activity;
    private DeviceListioner deviceListioner;
    private static final String TAG="TAG";
    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel
     * @param activity activity associated with the receiver
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
                                       Activity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    public void setDeviceListioner(DeviceListioner deviceListioner){
        this.deviceListioner=deviceListioner;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            Log.e(TAG,"\"=============devices size is===========ssssssssssss");
            if (manager != null) {
                manager.requestPeers(channel, (PeerListListener) activity);
            }
            Log.d(TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) {
                return;
            }
            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            Log.d(TAG, "P2P is begin to connected");
            if (networkInfo.isConnected()) {
                // we are connected with the other device, request connection
                // info to find group owner IP
                Log.d(TAG, "P2P is connected");
                manager.requestConnectionInfo(channel, (WifiP2pManager.ConnectionInfoListener) activity);
            } else {
                // It's a disconnect
                this.deviceListioner.onDeviceDisconnected();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        }
    }

    interface DeviceListioner{
        void onDeviceDisconnected();
    }
}
