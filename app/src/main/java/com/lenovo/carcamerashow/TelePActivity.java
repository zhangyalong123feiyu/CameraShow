package com.lenovo.carcamerashow;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;

public class TelePActivity extends Activity {
    public static SocketLive socketLive;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_telepl);
        initView();
    }

    private void initView() {
        findViewById(R.id.call).setOnClickListener(v -> {
            startActivity(new Intent(this,MainActivity.class));
        });

        findViewById(R.id.back).setOnClickListener(v -> {
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        connectServer();
    }

    private void connectServer() {
        socketLive = new SocketLive();
        socketLive.start();
        Log.e("TAG","begin to connect server");
    }
}
