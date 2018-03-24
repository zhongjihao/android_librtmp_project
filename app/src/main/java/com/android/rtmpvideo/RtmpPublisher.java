package com.android.rtmpvideo;

/**
 * Created by zhongjihao on 04/03/18.
 */

import android.util.Log;

public class RtmpPublisher {
    private static final String TAG = "RtmpPublisher";
    private long cPtr;
    private int timeOffset;

    private RtmpPublisher(){
        cPtr = 0;
        timeOffset = 0;
    }

    public static RtmpPublisher newInstance() {
        return new RtmpPublisher();
    }

    public int init(String url, String rtmpLogPath) {
        //初始化
        long ret =  RtmpJni.initRtmp(url, rtmpLogPath);
        if(ret == 0) {
            Log.e(TAG, "===zhongjihao=====Rtmp连接失败====");
            return 0;
        }
        cPtr = ret;
        return 1;
    }

    public void sendSpsAndPps(final byte[] sps, final int spsLen, final byte[] pps, final int ppsLen, int timeOffset){
        if(cPtr != 0){
            this.timeOffset = timeOffset;
            RtmpJni.sendSpsAndPps(cPtr,sps, spsLen, pps, ppsLen);
        }
    }

    public void sendAVCFrame(final byte[] frame, final int len, final int timestamp){
        if(timestamp - timeOffset < 0)
            return;
        if(cPtr != 0)
            RtmpJni.sendVideoFrame(cPtr,frame, len, timestamp);
    }

    public void sendAacSpec(final byte[] data, final int len){
        if(cPtr != 0)
            RtmpJni.sendAacSpec(cPtr,data, len);
    }

    public void sendAacData(final byte[] data, final int len, final int timestamp){
        if(timestamp - timeOffset < 0)
            return;
        if(cPtr != 0)
            RtmpJni.sendAacData(cPtr,data, len, timestamp);
    }

    public void stopRtmpPublish(){
        try {
            if(cPtr != 0)
                RtmpJni.stopRtmp(cPtr);
        }finally {
            cPtr = 0;
            timeOffset = 0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(cPtr != 0){
            stopRtmpPublish();
        }
    }
}
