package com.lenovo.carcamerashow;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.util.Log;


public class WifiP2PUtil {

    private WifiP2plistioner wifiP2plistioner;

    public void setWifiP2pListioner(WifiP2plistioner wifiP2plistioner){
        this.wifiP2plistioner= wifiP2plistioner;
    }
    public void discoverDevice(Context context,WifiP2pManager manager, WifiP2pManager.Channel channel){
        manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
//                Toast.makeText(context, "Discovery Initiated",
//                        Toast.LENGTH_SHORT).show();
                Log.e("TAG","-------------Discovery Initiated");
                wifiP2plistioner.onDiscoverDeviceSuccess();

            }
            @Override
            public void onFailure(int reasonCode) {

//                Toast.makeText(context, "Discovery Failed : " + reasonCode,
//                        Toast.LENGTH_SHORT).show();
                Log.e("TAG","-------------Discovery error is"+reasonCode);
                wifiP2plistioner.onDiscoverDevicesFailed();
            }
        });
    }

    public void createP2PGroup(Context context, WifiP2pManager manager, WifiP2pManager.Channel channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            manager.createGroup(channel, new WifiP2pManager.ActionListener() {

                @Override
                public void onSuccess() {
                    wifiP2plistioner.onCreateGroupSuccess();
                    Log.e("TAG","-------------group create is success");
                }

                @Override
                public void onFailure(int reason) {
                    wifiP2plistioner.onCreateGroupFailed();
                    Log.e("TAG","-------------group create error is"+reason);
                }
            });

        }
//        new Thread(() -> {
//            int count=1;
//            while (count<=2){
//                try {
//                    if (count==1){
//                        clientScoket1=serverSocket.accept();
//                    }else {
//                        clientScoket2=serverSocket.accept();
//                    }
//
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//                count++;
//            }
//
//        }).start();

    }


    public void removeGroup(WifiP2pManager manager, WifiP2pManager.Channel channel){
        manager.removeGroup(channel, new WifiP2pManager.ActionListener() {
            @Override
            public void onFailure(int reasonCode) {
                Log.e("TAG", "remove group failed:" + reasonCode);
            }
            @Override
            public void onSuccess() {
                Log.e("TAG", "remove group success :" );
            }
        });
    }

    public  boolean initP2p(Context context) {
        // Device capability definition check
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)) {
            Log.e("TAG", "Wi-Fi Direct is not supported by this device.");
            return false;
        }
        // Hardware capability check
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            Log.e("TAG", "Cannot get Wi-Fi system service.");
            return false;
        }
        if (!wifiManager.isP2pSupported()) {
            Log.e("TAG", "Wi-Fi Direct is not supported by the hardware or Wi-Fi is off.");
            return false;
        }
        WifiP2pManager manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        if (manager == null) {
            Log.e("TAG", "Cannot get Wi-Fi Direct system service.");
            return false;
        }
        WifiP2pManager.Channel channel = manager.initialize(context, context.getMainLooper(), null);
        if (channel == null) {
            Log.e("TAG", "Cannot initialize Wi-Fi Direct.");
            return false;
        }
        return true;
    }


    public interface WifiP2plistioner{
       void onCreateGroupSuccess();
       void onCreateGroupFailed();

       void onDiscoverDeviceSuccess();
       void onDiscoverDevicesFailed();
    }

}
