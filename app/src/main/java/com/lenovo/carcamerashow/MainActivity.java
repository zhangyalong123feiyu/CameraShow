package com.lenovo.carcamerashow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

public class MainActivity extends AppCompatActivity implements SocketLive.SocketCallback,SurfaceHolder.Callback{

    private Camera2TextureView remoteTextureView;
    private DecodePlayerLiveH264 decodePlayerLiveH264;
    private Camera2TextureView localTextureView;
    private SocketLive socketLive;
    private MediaPlayer player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(1920,1280);
        connectServer();
        initRemoteTextureView();
        initVideoView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initVideoView() {
        SurfaceView surface = findViewById(R.id.videoSurface);
        SurfaceHolder surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(this);

//        surface.setOnTouchListener(new View.OnTouchListener() {
//            private float downX;
//            private float downY;
//
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction()) {
//                    case MotionEvent.ACTION_DOWN:
//                        //按下位置
//                        downX = event.getRawX();
//                        downY = event.getRawY();
//
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//
//                        float moveX = event.getRawX();
//                        float moveY = event.getRawY();
//
//                        float dx = moveX - downX;
//                        float dy = moveY - downY;
//
//                        float tX = v.getTranslationX() + dx;
//                        float tY = v.getTranslationY() + dy;
//
//                        v.setTranslationX(tX);
//                        v.setTranslationY(tY);
//
//                        // 下一次按下位置
//                        downX = event.getRawX();
//                        downY = event.getRawY();
//                        break;
//                    case MotionEvent.ACTION_UP:
//
//                        //复位
////                        v.setTranslationX(0);
////                        v.setTranslationY(0);
//                        break;
//                }
//
//                return true;
//            }
//        });
    }

    private void initView(int viewWidth,int viewHeight) {
        localTextureView = findViewById(R.id.localSurfaceView);

        remoteTextureView = findViewById(R.id.remoteTextureView);
        remoteTextureView.setRotation(90);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) remoteTextureView.getLayoutParams();
        params.width = viewWidth;
        params.height =viewHeight;
        params.gravity= Gravity.CENTER;
        remoteTextureView.setLayoutParams(params);
    }

    public void frontCamera(View view){
        localTextureView.setVisibility(View.VISIBLE);
        remoteTextureView.setVisibility(View.GONE);
        socketLive.close();
        localTextureView.openCamera();

    }

    public void remoteCamera(View view){
        localTextureView.setVisibility(View.GONE);
        remoteTextureView.setVisibility(View.VISIBLE);
        socketLive.start();
        localTextureView.closeCamera();

    }
    private void connectServer() {
        socketLive=new SocketLive(this);
        socketLive.start();
    }

    private void initRemoteTextureView() {


        remoteTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                Surface remoteSurface = new Surface(surface);

                decodePlayerLiveH264 = new DecodePlayerLiveH264();
                decodePlayerLiveH264.initDecoder(remoteSurface);
                Log.e("TAG","width is -------"+width+"height--------"+height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });

    }

    @Override
    public void callBack(byte[] data) {
        if (decodePlayerLiveH264 != null) {
            decodePlayerLiveH264.callBack(data);
        }
        if (!player.isPlaying()){
            player.start();
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDisplay(holder);
        //设置显示视频显示在SurfaceView上
        try {
            player.setDataSource(this, Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.video));
            player.prepare();
            player.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {

    }
}