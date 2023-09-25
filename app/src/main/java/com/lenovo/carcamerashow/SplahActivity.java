package com.lenovo.carcamerashow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SplahActivity extends Activity implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.PeerListListener, WifiP2PUtil.WifiP2plistioner, WiFiDirectBroadcastReceiver.DeviceListioner{
    private final IntentFilter intentFilter = new IntentFilter();
    private WiFiDirectBroadcastReceiver receiver = null;
    private WifiP2PUtil wifiP2PUtil;
    private WifiP2pManager manager;
    private WifiP2pManager.Channel channel;
    private int lastScanCount = 0;
    private List<WifiP2pDevice> deviceslist = new ArrayList();
    private SharedHelper sharedHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        EditText edit = findViewById(R.id.edit);
        findViewById(R.id.go).setOnClickListener(v -> {
            String editInfo = edit.getText().toString().trim();
            if (!TextUtils.isEmpty(editInfo)){
                SocketLive.webIp=editInfo;
            }

            startActivity(new Intent(this,TelePActivity.class));
        });
//        initWifiP2P();
//        receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
//        registerReceiver(receiver, intentFilter);
//        receiver.setDeviceListioner(this);
//
//        sharedHelper=new SharedHelper(this);
//        SocketLive.webIp=sharedHelper.readIp();
//        if (TextUtils.isEmpty(SocketLive.webIp)){
//            Toast.makeText(this,"请等待网络连接",Toast.LENGTH_LONG).show();
//        }
    }

    public void call(View view){
        startActivity(new Intent(this,TelePActivity.class));
    }
    private void initWifiP2P() {
        createIntentFileter();
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(this, getMainLooper(), null);
        wifiP2PUtil = new WifiP2PUtil();
        wifiP2PUtil.setWifiP2pListioner(this);
        if (!wifiP2PUtil.initP2p(this)) {
            Toast.makeText(this, "该设备不支持WifiP2P", Toast.LENGTH_LONG).show();
        }
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                wifiP2PUtil.discoverDevice(getApplicationContext(), manager, channel);
            }
        }, 1000, 8000);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                connectClientDevice("a6:04:50:b2:66:39");
            }
        }, 1000, 2000);
    }
    private void createIntentFileter() {
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            SocketLive.webIp=wifiP2pInfo.groupOwnerAddress.getHostAddress();
            Log.e("TAG","host address is ======="+SocketLive.webIp);
            Log.e("TAG","socket live is empty");
            sharedHelper.saveIp(SocketLive.webIp);
            Toast.makeText(this,"网络已成功连接",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {

        int scanCount = wifiP2pDeviceList.getDeviceList().size();
        if (lastScanCount != scanCount) {
            lastScanCount = scanCount;
        } else {
            return;
        }
        Log.e("TAG", "wifiDevice list is========" + wifiP2pDeviceList.getDeviceList().size());
        Collection<WifiP2pDevice> devices = wifiP2pDeviceList.getDeviceList();
        deviceslist.addAll(devices);

    }

    @Override
    public void onDeviceDisconnected() {
         sharedHelper.saveIp("");
    }

    @Override
    public void onCreateGroupSuccess() {

    }

    @Override
    public void onCreateGroupFailed() {

    }

    @Override
    public void onDiscoverDeviceSuccess() {

    }

    @Override
    public void onDiscoverDevicesFailed() {

    }

    @SuppressLint("MissingPermission")
    private void connectClientDevice(String address) {
        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = address;
        config.wps.setup = WpsInfo.PBC;
        manager.connect(channel, config, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {

                Log.e("TAG", "device connect success======================" + address);
//                Toast.makeText(MainActivity.this, "Devices Connect Success ",
//                        Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onFailure(int reason) {
                Log.e("TAG", "device connect failed======================");
//                Toast.makeText(MainActivity.this, "Connect failed. Retry.",
//                        Toast.LENGTH_SHORT).show();
            }
        });
    }
}
