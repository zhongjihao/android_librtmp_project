package com.android.rtmpvideo;

import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, VideoGather.CameraOperateCallback,MediaPublisher.ConnectRtmpServerCb{
    private final static String TAG = "MainActivity";
    private Button btnStart;
    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private SurfacePreview mSurfacePreview;
    private MediaPublisher mediaPublisher;
    private boolean isStarted;
    private boolean isRtmpConnected = false;
    private static final String rtmpUrl = "rtmp://192.168.1.101:1935/zhongjihao/myh264";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        isStarted = false;
        btnStart = (Button) findViewById(R.id.btn_start);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mSurfaceView.setKeepScreenOn(true);
        // 获得SurfaceView的SurfaceHolder
        mSurfaceHolder = mSurfaceView.getHolder();
        // 设置surface不需要自己的维护缓存区
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 为srfaceHolder添加一个回调监听器
        mSurfacePreview = new SurfacePreview(this);
        mSurfaceHolder.addCallback(mSurfacePreview);
        btnStart.setOnClickListener(this);

        String logPath = Environment
                .getExternalStorageDirectory()
                + "/" + "zhongjihao/rtmp.log";
        mediaPublisher = MediaPublisher.newInstance(rtmpUrl,logPath);
        mediaPublisher.setRtmpConnectCb(this);
        mediaPublisher.initMediaPublish();
    }

    private void codecToggle() {
        if (isStarted) {
            isStarted = false;
            if(isRtmpConnected){
                //停止编码 先要停止编码，然后停止采集
                mediaPublisher.stopEncoder();
                //停止音频采集
                mediaPublisher.stopAudioGather();
                //断开RTMP连接
                mediaPublisher.stopRtmpPublish();
            }
        } else {
            isStarted = true;
            //连接Rtmp流媒体服务器
            mediaPublisher.startRtmpPublish();
        }
        btnStart.setText(isStarted ? "停止" : "开始");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isStarted){
            isStarted = false;
            if(isRtmpConnected){
                //停止编码 先要停止编码，然后停止采集
                mediaPublisher.stopEncoder();
                //停止音频采集
                mediaPublisher.stopAudioGather();
                //断开RTMP连接
                mediaPublisher.stopRtmpPublish();
            }
        }
        //释放编码器
        if(mediaPublisher != null)
            mediaPublisher.release();
        mediaPublisher = null;
        VideoGather.getInstance().doStopCamera();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            finish();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_start:
                codecToggle();
                break;
        }
    }

    @Override
    public void cameraHasOpened() {
        VideoGather.getInstance().doStartPreview(this, mSurfaceHolder);
    }

    @Override
    public void cameraHasPreview(int width,int height,int fps) {
        //初始化视频编码器
        mediaPublisher.initVideoEncoder(width,height,fps);
    }

    @Override
    public void onConnectRtmp(final int ret) {
        isRtmpConnected = ret == 0 ? false : true;
        if(ret != 0){
            //采集音频
            mediaPublisher.startAudioGather();
            //初始化音频编码器
            mediaPublisher.initAudioEncoder();
            //启动编码
            mediaPublisher.startEncoder();
        }
        runOnUiThread(new Runnable(){
            @Override
            public void run() {
                if(ret == 0){
                    Log.e(TAG, "===zhongjihao=====Rtmp连接失败====");
                    //更新UI
                    Toast.makeText(MainActivity.this,"RTMP流媒体服务器连接失败,请检测网络或服务器是否启动!",Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
