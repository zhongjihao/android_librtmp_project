package com.android.rtmpvideo;

/**
 * Created by zhongjihao on 04/03/18.
 */

import android.util.Log;

import java.util.concurrent.LinkedBlockingQueue;

public class MediaPublisher {
    private static final String TAG = "MediaPublisher";
    private String rtmpUrl;
    private String rtmpLogPath;
    private LinkedBlockingQueue<Runnable> mRunnables = new LinkedBlockingQueue<>();
    private Thread workThread;

    private VideoGather mVideoGather;
    private AudioGather mAudioGather;
    private AVEncoder mAVEncoder;

    private RtmpPublisher mRtmpPublisher;
    private boolean isPublish;

    private volatile boolean loop;

    private ConnectRtmpServerCb rtmpConnectCb;
    public interface ConnectRtmpServerCb{
        public void onConnectRtmp(final int ret);
    }

    public static MediaPublisher newInstance(String url,String log) {
        return new MediaPublisher(url,log);
    }

    private MediaPublisher(String url,String log){
        rtmpUrl = url;
        rtmpLogPath = log;
    }

    public void setRtmpConnectCb( ConnectRtmpServerCb rtmpConnectCb){
        this.rtmpConnectCb = rtmpConnectCb;
    }

    public void initMediaPublish() {
        if (loop) {
            throw new RuntimeException("====zhongjihao====Media发布线程已经启动===");
        }
        mVideoGather = VideoGather.getInstance();
        mAudioGather = AudioGather.getInstance();
        mAudioGather.prepareAudioRecord();
        mAVEncoder = AVEncoder.newInstance();
        mRtmpPublisher = RtmpPublisher.newInstance();
        setListener();

        workThread = new Thread("publish-thread") {
            @Override
            public void run() {
                while (loop && !Thread.interrupted()) {
                    try {
                        Runnable runnable = mRunnables.take();
                        runnable.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                mRunnables.clear();
            }
        };

        loop = true;
        workThread.start();
    }

    /**
     * 初始化视频编码器
     */
    public void initVideoEncoder(int width,int height,int fps){
        mAVEncoder.initVideoEncoder(width,height,fps);
    }

    /**
     * 初始化音频编码器
     */
    public void initAudioEncoder(){
        mAVEncoder.initAudioEncoder(mAudioGather.getaSampleRate(),mAudioGather.getPcmForamt(),mAudioGather.getaChannelCount());
    }

    /**
     * 开始音频采集
     */
    public void startAudioGather() {
        mAudioGather.startRecord();
    }

    /**
     * 停止音频采集
     */
    public void stopAudioGather() {
        mAudioGather.stopRecord();
    }

    /**
     * 释放
     */
    public void release() {
        mAVEncoder.release();
        mAudioGather.release();
        loop = false;
        if (workThread != null) {
            workThread.interrupt();
            workThread = null;
        }
    }

    public void startRtmpPublish(){
        if (isPublish) {
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                //初始化
                int ret = mRtmpPublisher.init(rtmpUrl, rtmpLogPath);
                rtmpConnectCb.onConnectRtmp(ret);
                if (ret == 0) {
                    Log.e(TAG, "===zhongjihao=====Rtmp连接失败====");
                    return;
                }
                isPublish = true;
            }
        };
        mRunnables.add(runnable);
    }

    public void stopRtmpPublish(){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "====zhongjihao====停止发布=====");
                mRtmpPublisher.stopRtmpPublish();
                isPublish = false;
            }
        };

        mRunnables.add(runnable);
    }

    /**
     * 开始编码
     */
    public void startEncoder() {
        mAVEncoder.start();
    }

    /**
     * 停止编码
     */
    public void stopEncoder() {
        mAVEncoder.stop();
    }

    private void setListener() {
        mVideoGather.setCallback(new VideoGather.Callback() {
            @Override
            public void videoData(byte[] data) {
                mAVEncoder.putVideoData(data);
            }
        });

        mAudioGather.setCallback(new AudioGather.Callback() {
            @Override
            public void audioData(byte[] data) {
                mAVEncoder.putAudioData(data);
            }
        });

        mAVEncoder.setCallback(new AVEncoder.Callback() {
            @Override
            public void outputVideoSpsPps(final byte[] sps,final int spslen,final byte[] pps,final int ppslen,final int nTimeStamp) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "====zhongjihao====outputVideoSpsPps=====spslen: "+spslen+"   ppslen: "+ppslen);
                        mRtmpPublisher.sendSpsAndPps(sps, spslen, pps, ppslen,nTimeStamp);
                    }
                };
                try {
                    mRunnables.put(runnable);
                } catch (InterruptedException e) {
                    Log.e(TAG, "====zhongjihao====outputVideoSpsPps=====error: "+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void outputVideoFrame(final byte[] nalu,final int len,final int nTimeStamp){
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "====zhongjihao====outputVideoFrame=====naluLen: "+len);
                        mRtmpPublisher.sendAVCFrame(nalu, len, nTimeStamp);
                    }
                };
                try {
                    mRunnables.put(runnable);
                } catch (InterruptedException e) {
                    Log.e(TAG, "====zhongjihao====outputVideoFrame=====error: "+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void outputAudioSpecConfig(final byte[] aacSpec, final int len){
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mRtmpPublisher.sendAacSpec(aacSpec, len);
                    }
                };
                try {
                    mRunnables.put(runnable);
                } catch (InterruptedException e) {
                    Log.e(TAG, "====zhongjihao====outputAudioSpecConfig=====error: "+e.toString());
                    e.printStackTrace();
                }
            }

            @Override
            public void outputAudioData(final byte[] aac, final int len,final int nTimeStamp) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        mRtmpPublisher.sendAacData(aac, len, nTimeStamp);
                    }
                };
                try {
                    mRunnables.put(runnable);
                } catch (InterruptedException e) {
                    Log.e(TAG, "====zhongjihao====outputAudioData=====error: "+e.toString());
                    e.printStackTrace();
                }
            }
        });
    }
}
