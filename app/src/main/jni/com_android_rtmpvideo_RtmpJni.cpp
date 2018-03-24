//
// Created by zhongjihao on 18-2-9.
//

#define LOG_TAG "RTMP-JNI"

#include "com_android_rtmpvideo_RtmpJni.h"
#include "rtmpjni/CRtmpWrap.h"
#include <string.h>

#include <android/log.h>

#define ALOGD(...) __android_log_print(ANDROID_LOG_DEBUG,LOG_TAG,__VA_ARGS__)
#define ALOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


static FILE* pLogfile = NULL;

JNIEXPORT jlong JNICALL Java_com_android_rtmpvideo_RtmpJni_initRtmp(JNIEnv* env,jclass jcls,jstring jurl,jstring jlogpath)
{
    ALOGD("%s: E: ====zhongjihao====Rtmp NetConnect======",__FUNCTION__);
    int ret = 0;
    const char* url = env->GetStringUTFChars(jurl,NULL);
    const char* logpath = env->GetStringUTFChars(jlogpath,NULL);
    CRtmpWrap* pRtmpH264 = new CRtmpWrap;
    pLogfile = fopen(logpath,"w+");
    if(!pLogfile) {
        ALOGE("%s: ======zhongjihao=====open: %s",__FUNCTION__,strerror(errno));
        goto failed;
    }
    ALOGD("%s: =====zhongjihao=====uri: %s",__FUNCTION__,url);
    //初始化并连接到服务器
    ret = pRtmpH264->RTMPAV_Connect(url,pLogfile);
    if(ret == 1) { //连接RTMP服务器OK
        env ->ReleaseStringUTFChars(jurl, url);
        env ->ReleaseStringUTFChars(jlogpath,logpath);
        ALOGD("%s: X: ====zhongjihao=======Rtmp NetConnect=====ret: %d",__FUNCTION__,ret);
        return reinterpret_cast<long> (pRtmpH264);
    }

failed:
    delete pRtmpH264;
    if(pLogfile)
        fclose(pLogfile);
    pLogfile = NULL;
    env ->ReleaseStringUTFChars(jurl, url);
    env ->ReleaseStringUTFChars(jlogpath,logpath);
    ALOGD("%s: X: ====zhongjihao====Rtmp NetConnect====ret: %d",__FUNCTION__,ret);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_android_rtmpvideo_RtmpJni_sendSpsAndPps(JNIEnv* env,jclass jcls,jlong cptr,jbyteArray jsps, jint spsLen, jbyteArray jpps, jint ppsLen)
{
    ALOGD("%s: E: =====zhongjihao=====RTMP send video h264 sps pps=====",__FUNCTION__);
    jbyte* sps = env->GetByteArrayElements(jsps, NULL);
    jbyte* pps = env->GetByteArrayElements(jpps, NULL);

    unsigned  char* spsData = (unsigned char*)sps;
    unsigned  char* ppsData = (unsigned char*)pps;
    CRtmpWrap* rtmp = reinterpret_cast<CRtmpWrap *> (cptr);
    int ret = rtmp->SendVideoSpsPps(ppsData,ppsLen,spsData,spsLen);
    env->ReleaseByteArrayElements(jsps, sps, 0);
    env->ReleaseByteArrayElements(jpps, pps, 0);
    ALOGD("%s: X: =====zhongjihao====send video h264 sps pps=====ret: %d",__FUNCTION__,ret);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_android_rtmpvideo_RtmpJni_sendVideoFrame(JNIEnv* env,jclass jcls,jlong cptr,jbyteArray jframe, jint len,jint time)
{
    ALOGD("%s: E: ====zhongjihao=====RTMP send video h264 frame====大小: %d, 时间戳: %d",__FUNCTION__,len,(unsigned int)time);
    jbyte* frame = env->GetByteArrayElements(jframe, NULL);
    unsigned char* data = (unsigned char*)frame;
    /*去掉StartCode帧界定符*/
    if (data[2] == 0x00) {/*00 00 00 01*/
        data += 4;
        len -= 4;
    } else if (data[2] == 0x01) {/*00 00 01*/
        data += 3;
        len -= 3;
    }

    //提取NALU Header中type字段,Nalu头一个字节，type是其后5bit
    int type = data[0] & 0x1f;
    int bKeyframe  = (type == 0x05) ? TRUE : FALSE;
    CRtmpWrap* rtmp = reinterpret_cast<CRtmpWrap *> (cptr);
    int ret = rtmp->SendH264Packet(data,len,bKeyframe,(unsigned int)time);
    env->ReleaseByteArrayElements(jframe, frame, 0);
    ALOGD("%s: X: ===zhongjihao=====RTMP send video h264 frame====大小: %d, 时间戳: %d,  ret: %d",__FUNCTION__,len,(unsigned int)time,ret);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_android_rtmpvideo_RtmpJni_sendAacSpec(JNIEnv* env, jclass jcls,jlong cptr, jbyteArray jaacSpec, jint jlen)
{
    ALOGD("%s: E: ===zhongjihao=====RTMP send audio Aac SpecificConfig=====",__FUNCTION__);
    jbyte* aacSpec = env->GetByteArrayElements(jaacSpec, NULL);
    unsigned  char* aacSpecData = (unsigned char*)aacSpec;

    CRtmpWrap* rtmp = reinterpret_cast<CRtmpWrap *> (cptr);
    int ret = rtmp->SendAacSpec(aacSpecData,(int)jlen);
    env->ReleaseByteArrayElements(jaacSpec, aacSpec, 0);
    ALOGD("%s: X: ===zhongjihao=====RTMP send audio Aac SpecificConfig=====ret: %d",__FUNCTION__,ret);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_android_rtmpvideo_RtmpJni_sendAacData(JNIEnv* env, jclass jcls, jlong cptr,jbyteArray jaccFrame, jint jlen, jint jtimestamp)
{
    ALOGD("%s: E: ====zhongjihao====RTMP send audio Aac Data====len: %d",__FUNCTION__,(int)jlen);
    jbyte* accFrame = env->GetByteArrayElements(jaccFrame, NULL);
    unsigned  char* accFrameData = (unsigned char*)accFrame;

    if((int)jlen>=7)
        ALOGD("%s: ======zhongjihao=====aac[0]: %d,aac[1]: %d, aac[2]: %d, aac[3]: %d, aac[4]: %d, aac[5]: %d,aac[6]: %d, timestamp: %d",__FUNCTION__,
                                     accFrameData[0],accFrameData[1],accFrameData[2],
                                     accFrameData[3],accFrameData[4],accFrameData[5],accFrameData[6],(int)jtimestamp);
    CRtmpWrap* rtmp = reinterpret_cast<CRtmpWrap *> (cptr);
    int ret = rtmp->SendAacData(accFrameData,(int)jlen,(int)jtimestamp);
    env->ReleaseByteArrayElements(jaccFrame, accFrame, 0);
    ALOGD("%s: X: ====zhongjihao====RTMP send audio Aac Data====ret: %d",__FUNCTION__,ret);
    return ret;
}

JNIEXPORT jint JNICALL Java_com_android_rtmpvideo_RtmpJni_stopRtmp(JNIEnv* env,jclass jcls,jlong cptr)
{
    ALOGD("%s: E: =====zhongjihao=====RTMP断开======",__FUNCTION__);
    //断开RTMP连接并释放相关资源
    CRtmpWrap* rtmp = reinterpret_cast<CRtmpWrap *> (cptr);
    delete rtmp;
    if(pLogfile)
        fclose(pLogfile);
    pLogfile = NULL;
    ALOGD("%s: X: =====zhongjihao=====RTMP断开======",__FUNCTION__);
    return 0;
}