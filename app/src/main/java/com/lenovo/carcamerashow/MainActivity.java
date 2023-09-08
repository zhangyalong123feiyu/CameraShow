package com.lenovo.carcamerashow;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SocketLive.SocketCallback, SurfaceHolder.Callback {

    private TextureView remoteTextureView;
    private DecodePlayerLiveH264 decodePlayerLiveH264;
    private Camera2TextureView localTextureView;
    private SocketLive socketLive;
    private MediaPlayer player;
    private boolean isFrontCamera = true;
    private RelativeLayout userLayout;

    private boolean canIn = true;
    private ImageView carCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);//必须在setContentView方法前执行
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView(2400, 1080);
//        connectServer();
        initRemoteTextureView();
        connectRemoteView();

        this.socketLive=TelePActivity.socketLive;
        TelePActivity.socketLive.setSocketBack(this);
    }

    private void connectRemoteView() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                initVideoView();
                                userLayout.setVisibility(View.GONE);
                            }
                        });

                    }
                });
            }
        }, 3000);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initVideoView() {
        SurfaceView surface = findViewById(R.id.videoSurface);
        surface.setVisibility(View.VISIBLE);
        SurfaceHolder surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(this);
    }

    private void initView(int viewWidth, int viewHeight) {
        ImageView handleUp = findViewById(R.id.handleUp);
        handleUp.setOnClickListener(v -> {
            finish();
//            android.os.Process.killProcess(android.os.Process.myPid());
        });
        localTextureView = findViewById(R.id.localSurfaceView);
        remoteTextureView = findViewById(R.id.remoteTextureView);
        remoteTextureView.setRotation(90);
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) remoteTextureView.getLayoutParams();
        params.width = viewWidth;
        params.height = viewHeight;
        params.gravity = Gravity.CENTER;
        remoteTextureView.setLayoutParams(params);
        carCamera=findViewById(R.id.carCamera);
        findViewById(R.id.switchCamera).setOnClickListener(v -> {
            openCamera();
            carCamera.setSelected(false);
        });
        carCamera.setOnClickListener(v -> {
            localTextureView.setVisibility(View.GONE);
            remoteTextureView.setVisibility(View.VISIBLE);
//            socketLive.start();
            localTextureView.closeCamera();
            carCamera.setSelected(true);
        });

        userLayout = findViewById(R.id.userLayout);
    }

    private void openCamera() {

        if (isFrontCamera) {
            localTextureView.setVisibility(View.VISIBLE);
            remoteTextureView.setVisibility(View.GONE);
            if (socketLive != null) {
//                socketLive.close();
            }
            localTextureView.closeCamera();
            localTextureView.openCamera("front");
            isFrontCamera = false;
        } else {
            localTextureView.setVisibility(View.VISIBLE);
            remoteTextureView.setVisibility(View.GONE);
            if (socketLive != null) {
//                socketLive.close();
            }
            localTextureView.closeCamera();
            localTextureView.openCamera("back");
            isFrontCamera = true;

        }

    }


    public void remoteCamera(View view) {
        localTextureView.setVisibility(View.GONE);
        remoteTextureView.setVisibility(View.VISIBLE);
        socketLive.start();
        localTextureView.closeCamera();

    }

    private void connectServer() {
        socketLive = new SocketLive(this);
        socketLive.start();
        Log.e("TAG","begin to connect server");
    }

    private void initRemoteTextureView() {


        remoteTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

                Surface remoteSurface = new Surface(surface);
                decodePlayerLiveH264 = new DecodePlayerLiveH264();


                decodePlayerLiveH264.initDecoder(remoteSurface);
                openCamera();
                Log.e("TAG", "width is -------" + width + "height--------" + height);
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
    protected void onStop() {
        super.onStop();
        if (player!=null){
            player.stop();
            player.release();
            player=null;
            socketLive.close();
        }

    }

    @Override
    public void callBack(byte[] data) {
        if (player != null) {
            if (!player.isPlaying()) {
                player.start();
            }
        }
//        if (canIn) {
//            canIn = false;
//            connectRemoteView();
//        }

        if (decodePlayerLiveH264 != null) {
            decodePlayerLiveH264.callBack(data);
        }


    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setDisplay(holder);
        //设置显示视频显示在SurfaceView上
        try {
            player.setDataSource(this, Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video));
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