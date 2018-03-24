/*************************************************************************
    > File Name: CRtmpWrap.h
    > Author: zhongjihao
    > Mail: zhongjihao100@163.com 
    > Created Time: 2018年02月09日 星期五 10时14分30秒
 ************************************************************************/

#ifndef CRTMP_WRAP_H
#define CRTMP_WRAP_H

#include "../rtmpvedio/rtmpdump/librtmp/rtmp_sys.h"
#include "../rtmpvedio/rtmpdump/librtmp/log.h"
#include "../rtmpvedio/rtmpdump/librtmp/rtmp.h"

//定义包头长度，RTMP_MAX_HEADER_SIZE=18
#define RTMP_HEAD_SIZE   (sizeof(RTMPPacket) + RTMP_MAX_HEADER_SIZE)


//本类用于将内存中的H.264数据推送至RTMP流媒体服务器
class CRtmpWrap
{
private:
	RTMP* m_pRtmp;                   //RTMP协议对象

private:
	/**
	 * 发送RTMP数据包
	 * @param nPacketType 数据类型
	 * @param data 存储数据内容
	 * @param size 数据大小
	 * @param nTimestamp 当前包的时间戳
	 * @成功则返回 1 , 失败则返回 0
	*/
	int SendPacket(unsigned int nPacketType,unsigned char* data,unsigned int size,unsigned int nTimestamp);

public:
	CRtmpWrap();
	~CRtmpWrap();
	/**
	 * 初始化并连接到RTMP服务器
	 * @param url 服务器上对应webapp的地址
	 * @param logfile log文件
	 * @成功则返回 1 , 失败则返回 0
	*/
	int RTMPAV_Connect(const char* url,FILE* logfile);

	/**
	 * 发送H264数据帧
	 * @param data 存储数据帧内容
	 * @param size 数据帧的大小
	 * @param bIsKeyFrame 记录该帧是否为关键帧
	 * @param nTimeStamp 当前帧的时间戳
	 * @成功则返回 1 , 失败则返回 0
	*/
	int SendH264Packet(unsigned char* data,unsigned int size,int bIsKeyFrame,unsigned int nTimeStamp);

	/**
	 * 发送视频的sps和pps信息
	 * @param pps 存储视频的pps信息
	 * @param pps_len 视频的pps信息长度
	 * @param sps 存储视频的sps信息
	 * @param sps_len 视频的sps信息长度
	 * @成功则返回 1 , 失败则返回 0
	*/
	int SendVideoSpsPps(unsigned char* pps,int pps_len,unsigned char* sps,int sps_len);

	/**
	 * 发送音频关键帧
	 * @param data 音频关键帧信息
	 * @param len  音频关键帧信息长度
	 * @成功则返回 1 , 失败则返回 0
    */
	int SendAacSpec(unsigned char* data, int len);

	/**
     * 发送音频数据
     * @param data 音频帧信息
	 * @param len  音频帧信息长度
	 * @param nTimeStamp 当前帧的时间戳
	 * @成功则返回 1 , 失败则返回 0
     */
	int SendAacData(unsigned char* data, int len,unsigned int nTimeStamp);

	/**
	 * 断开连接，释放相关的资源
	*/
	void RTMPAV_Close();
};

#endif


