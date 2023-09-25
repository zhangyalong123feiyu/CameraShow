package com.lenovo.carcamerashow;

import android.util.Log;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

//通话 服务端
public class SocketLive {
    private static final String TAG = "zyl";
    private SocketCallback socketCallback;
    private ExecutorService service;
    public static String webIp="192.168.1.101"; //
    MyWebSocketClient myWebSocketClient;

    public SocketLive(SocketCallback socketCallback ) {
        this.socketCallback = socketCallback;

        service = Executors.newFixedThreadPool(5);

    }

    public SocketLive() {
    }

    public void setSocketBack(SocketCallback callback){
        this.socketCallback=callback;
    }
    public void close() {
        try {
            if(myWebSocketClient != null) myWebSocketClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public void start() {
        try {
            URI url = new URI("ws://"+webIp+":12005");
            myWebSocketClient = new MyWebSocketClient(url);
            myWebSocketClient.connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class MyWebSocketClient extends WebSocketClient {

        public MyWebSocketClient(URI serverURI) {
            super(serverURI);
        }

        @Override
        public void onOpen(ServerHandshake serverHandshake) {
            Log.i(TAG, "客户端 打开 socket  onOpen: ");
        }

        @Override
        public void onMessage(String s) {
        }

        private ReentrantLock lock = new ReentrantLock();

        @Override
        public void onMessage(ByteBuffer bytes) {
            byte[] buf = new byte[bytes.remaining()];
            bytes.get(buf);
            if (socketCallback!=null){
                socketCallback.callBack(buf);
            }
        }

        @Override
        public void onClose(int i, String s, boolean b) {
            Log.i(TAG, "onClose: ");
        }

        @Override
        public void onError(Exception e) {
            Log.i(TAG, "onError: ", e);
        }
    }

    public void sendData(byte[] bytes) {
        if (myWebSocketClient != null && myWebSocketClient.isOpen()) {
            Log.i("TAG", "sendData:  " + Arrays.toString(bytes));
            myWebSocketClient.send(bytes);
        }
    }
    public interface SocketCallback {
        void callBack(byte[] data);
    }
}

