package com.android.rtmpvideo;

import android.util.Log;

/**
 * Created by zhongjihao100@163.com on 18-2-7.
 */

public class RtmpJni {
    static {
        Log.d("RtmpJni", "====zhongjihao====add lib RTMP  start===");
        System.loadLibrary("rtmpJni");
        Log.d("RtmpJni", "====zhongjihao=====add lib RTMP end===");
    }

    public static final native long initRtmp(String url, String logpath);

    public static final native int sendSpsAndPps(long cptr,byte[] sps, int spsLen, byte[] pps, int ppsLen);

    public static final native int sendVideoFrame(long cptr,byte[] frame, int len, int timestamp);

    public static final native int sendAacSpec(long cptr,byte[] data, int len);

    public static final native int sendAacData(long cptr,byte[] data, int len, int timestamp);

    public static final native int stopRtmp(long cptr);
}
