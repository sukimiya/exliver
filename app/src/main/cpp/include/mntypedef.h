#ifndef _udx_typedef_h_
#define _udx_typedef_h_

#ifdef WIN32
#pragma pack( push, 1 )
	#ifndef UDXPACKED
	#define UDXPACKED 
	#endif
#else
	#ifndef UDXPACKED
	#define UDXPACKED	__attribute__((packed, aligned(1)))
	#endif
#endif


#define AUDIOFRAME_A	0//音频帧
#define VIDEOFRAME_I	1//I帧
#define VIDEOFRAME_P	2//P帧
#define DATAFRAME_I		3//数据帧

enum Video_Code_Type
{
	Video_Code_264,
		Video_Code_265
};

enum Audio_Code_Type
{
	Audio_Code_AAC,
	Audio_Code_OPUS,
	Audio_Code_ILBC,
	Audio_Code_G711,
	Audio_Code_G721
};


struct FrameType
{
	unsigned char type0:1;//区分MTS和IDM
	unsigned char type1:2;//音视频，数据
	unsigned char type2:3;//子类型，type2 = FTypeVer_1时帧头变成FrameType2
	unsigned char modestream:1;//是否是流式传输
	unsigned char noused0:1;
	unsigned int  sid;//流ID
	unsigned short sbid;//子流ID,short类型
	unsigned char jumpmode;//丢弃优先,//level 0.vIframe 1.1/2  2.1/4 3.1/6 4.1/8
}UDXPACKED;


#define MD_VIFRAME 0x10
#define MD_VPFRAME0	0x20
#define MD_VPFRAME1	0x40

//跳侦模式
#define MD_1_2	0x1
#define MD_1_4	0x2
#define MD_1_6	0x4
#define MD_1_8	0x8


//侦类型
#define ZN_IFRAME	VIDEOFRAME_I//I帧
#define ZN_PFRAME	VIDEOFRAME_P//P帧
#define ZN_VIFRAME	VIDEOFRAME_I//虚拟I帧
#define ZN_AFRAME	AUDIOFRAME_A//音频帧
#define ZN_DFRAME	DATAFRAME_I//数据帧

struct AVFrameHead //蛮牛视频帧头
{
	unsigned char MN[2];//固定的"MN"
	unsigned char Co;//公司代号
	unsigned char avframetype:4;//I,P,A侦类型,当是VIDEOFRAME_I,VIDEOFRAME_P
	unsigned char code:4;//音视频编码Video_Code_Type or Audio_Code_Type
	unsigned int pts;//时间戳
	unsigned short w;//宽
	unsigned short h;//高
	unsigned int rpts;//录像时间	
	unsigned int datalen;//数据长度	
}UDXPACKED;


struct Media_FrameInfo
{
	unsigned char JumpFrameMode;//跳侦模式,如果不支持跳侦，直接设置0
	unsigned char FrameType;//侦类型，ZN_IFRAME，....
	unsigned long MainIndex;//侦序号,当JumpFrameMode不为0时，必须指定侦序号，其他可 为0
	unsigned char bStart;//一侦开始标记，当发送的数据块，为完整的侦，bstart等于bend等于1（即是第一片，也是最后一片），如果是分片传输侦的时候，bstart = 1表示第一片，当bend =1表示最后一片
	unsigned char bLast;//一侦结束标记
	unsigned char codetype;//Video_Code_Type or Audio_Code_Type
	int FrameLen;//当前分片长度
	unsigned char * data;//当前分片数据
};

#ifdef WIN32
#pragma pack( pop)
#endif

#ifndef __cplusplus

typedef struct FrameType FrameType;

#endif

#define UdxFrameType FrameType
// #define UdxFrameType1 FrameType1

#endif