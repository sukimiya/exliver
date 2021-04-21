
// The following ifdef block is the standard way of creating macros which make exporting 
// from a DLL simpler. All files within this DLL are compiled with the FASTUDX_EXPORTS
// symbol defined on the command line. this symbol should not be defined on any project
// that uses this DLL. This way any other project whose source files include this file see 
// FASTUDX_API functions as being imported from a DLL, wheras this DLL sees symbols
// defined with this macro as being exported.

#ifndef FASTUDX_H
#define FASTUDX_H

#define UDXMAXVER	2
#define UDXSLAVER	206



// //// //// //// //// //// //// //// //
// 1.84 主要把内核改成了多线程，接收和发送在IO,逻辑上都区分开了，并发支持更好，响应速度提高
// 1.86	对新内核的一次各方面优化和修改BUG,较稳定版本
// 1.87 修改了定时器的bug,并对每个联接增加了，UdxTrackData,包括30个用户公共保存区，长度为sizeof(UINT64)*30
// 1.88 修改了64位编译的bug,增加静态64位lib,.a文件
// 1.90 扩展了最大窗口为16k，在高延迟，GM高速、高延迟网络吞吐量，增加了ACK分片功能。缺点（不支持早其1.90以前版本）
// 1.91 对拥塞控制进行了优化，包括ack发送频率调整，防止不必要的重传
// 1.92 优化内存池回收，其他小BUG调整，比如缓冲控制部分
// 1.93 去掉了文件传输接口的Accept方法，已经过时，弃用了，另外实现了cancel方法，以前版本没有实现,但是引入了联接超时事件不能触发的BUG
// 1.94 增加IMulcardUdx支持多个网络（网卡,3G卡)绑定一个UDX对象，进行单个流的收发，主要用在移动设备，车载系统中
// 1.95 修改1.93引入的联接超时不能触发事件的bug. 去掉几个重复弃用的变量
// 1.96 修改了内存COPY的效率问题，减少了一次COPY，本地提高5% CPU执行效率，及线程调度，包括联接释放过程，增加了稳定性
// 1.97 增强了文件输过程中，存在同样文件时，立即返回发送成功，另外修改了流量探测参数
// 1.98 IFastUdx接口增加了是否分片选项，另外把回调事件独立出来，完全不影响，数据包的组包处理。
// 1.991 增加了多线程并行计算策略，修复了在SetFloatSize超过16MB时，ACK超过1500字节的BUG，增加IMediaPush接口发送音视频，防止花屏,简化速度计算算法
// 1.992 修复非合并包填包BUG,增加了文件校验机制
// 1.993 修改linux下由于fork子进程时，时钟被提前释放问题
// 1.994 修改OnStreamNeedMoreData(this)->OnStreamNeedMoreData(this,needdata),增加IMediaPush回调OnMediaPushFrameEvent，当发送缓冲满时，提示音视频有多少侦缓冲，可通过侦数缓冲，减少延迟
//		 增加事件OnStreamFinalRelease，当联接被释放前，回调用于清理一些关联用户自定义数据
// 1.995 对发送队列过行了一些调整优化，对事件句柄管理进行调整，修改IMediaPush的包头结构把SID从short改成int,配合分布式传输
//		 调整缓冲出队入队规则，对内存池总量限制，增加log禁止功能，及对外输出功能
// 1.996 修正1.995中，为CBuffmaplist中优化发送队列引入流量突起的新bug。
// 1.997 修正ACK中分片问题
// 1.998 修改IMulcardUdx的实现，全面支持多卡
// 1.999 优化了包头，去掉了源ID,和中转ID，减少了4字节，由原来的包头14字节，降为10字节,另外优化了动态库作为ocx，activex退出问题。
//       优化了ack产生的大小，200KB流量产生的ACK总量不足2K, 10MB的流量ACK不足5KB
// 2.0   优化了统计周期，预测流量，增加了自动调整发送缓冲
// 2.002 包头采取加密算法，防止防火墙根据特征来限制UDP包。
// 2.003 增加了延迟关闭特性，能将在tcp.close之前的数据发送完成，然后释放tcp联接，对于发送一些信令非常有用(默认延迟10秒)。
// 2.004 增加了联接时协带扩展数据，长度小于1K
// 2.005 优化了断开过程（当联接断开时，立即删除UDX时，断开包可能没有发送出去，目前增加了三次重试机会，增加断开消息发送的成功率）
//

// 2.101 添加FEC的支持，兼容2.005，并且支持IPV6，把消息通道改成可以通过参数配置形成RTP+FEC方式，更加适合语音传输
// 2.102 从主通道移除FEC的支持，消息通道收发改用单独线程,继续支持FEC
// 2.103-4 拥塞算法优化，及收发线程重新规划，调整速率机制
// 2.105 优化4G的P2P，增加穿透成功率
// 2.106 数据通道增加FEC模式，FEC模式中，FEC块大小自动适应
// 2.107 修改发送速度策略，拥塞理论更一步清晰明确化，属于里程碑1.0
// 2.109 修改发送速度策略，拥塞理论更一步清晰明确化，属于里程碑2.0
// 2.200 优化速度，RTT计算策略，飞行数据精确控制,动态恢复
// // 
// 2.201 优化本地速度，修改内存
// 2.202 优化FEC解码速度，修改FEC为动态FEC，参数可配，也可以内部动态调整打下基础
// 2.203 优化FEC解码的时机，减少至少75%不必要的FEC纠错计算，因为纠错包接收后，可能在附近时间内会有迟到，乱序的数据包，这种情况下是不需要FEC纠错的
// 2.204 优化服务器并发，从500提升至至少5000联接，并发收发
// 2.205 优化了P2P联接过程，客户端与服务端，客户端同时发起P2P与TS，并完成P2P的自动切换。支持了自适应FEC参数（当FEC)开启后，此功能屌炸天
// 2.206 修复丢包超过30%左右时FEC内存泄露问题，减少内存占用

// MS VC++ 10.0 _MSC_VER = 1600
// MS VC++ 9.0 _MSC_VER = 1500
// MS VC++ 8.0 _MSC_VER = 1400
// MS VC++ 7.1 _MSC_VER = 1310
// MS VC++ 7.0 _MSC_VER = 1300
// MS VC++ 6.0 _MSC_VER = 1200
// MS VC++ 5.0 _MSC_VER = 1100


#ifdef WIN32
	#if _MSC_VER >= 1600 // for vc8, or vc9 vs 2008 ~ vs2013

#include <WinSock2.h>
#include <windows.h>

#include <MMSystem.h>
#include <assert.h>

#ifndef _cplusplus
#include <atlbase.h>
#endif

#include <fcntl.h>

#include <io.h>

typedef unsigned int UDP_LONG ;
typedef unsigned short UDP_SHORT ;
typedef BYTE UDP_BYTE ; 
typedef long UDX_LONG;
typedef unsigned long UUDX_LONG;

#pragma comment(lib,"ws2_32.lib")
#include <map>
#include <vector>
#include <string>
#include <list>
using namespace std;


	#else
		#include "udxos.h"
	#endif
#else
	#include "udxos.h"
#endif


enum ERROCODE
{
	//errocode : 0,成功，1，新的联接到来，2，远程拒绝联接,3超时
	UDX_CON_SUCCEED,
		UDX_CON_NEWCON,
		UDX_CON_EJECT,
		UDX_CON_TIMEOUT,
		UDX_CON_SERVEREXIT
};


#include "mntypedef.h"

#define UDX_P2P_REGISTER_MSG_LINK_FLAG	1//UDX P2P 消息联接
#define UDX_P2P_DATATRANS_LINK_FLAG	2//UDX P2P 数据联接

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


typedef void* UFASTUDX;
typedef void* UDXTCP;

typedef void (CALLBACK UDXPRC)(int eventtype, int erro, UDXTCP s, BYTE* pData, int len);
typedef void (CALLBACK UDXP2PPRC)(char* user1,char* user2,UINT64 dwuser);

typedef UDXPRC FAR *LPUDXPRC;
typedef UDXP2PPRC FAR *LPUDXP2PPRC;



	//对地址的一些转换，一般不必使用
class IUdxTools
{
public:
	virtual UINT64 Addr2Int64(SOCKADDR* pAddr,UDP_LONG streamId) = 0;//将地址与一个WORD转化为一个64位常量
	virtual void TraceAddr(SOCKADDR* pAddr) = 0;//打印
	virtual void GetSpeedStr(char * buff,UINT64 speed  ) = 0;//得到发送或接收的速度字符串
	virtual void Trace(const char* strLog) = 0;
	virtual int GetMD5( BYTE* pData,int len,char * pStrMD5Out ) = 0;
	virtual UDP_LONG GetHashLong(char* strKey) = 0;
	virtual void LimitDate(int y,int m,int d) = 0;
	virtual UDP_SHORT CheckSum(BYTE* pData,int len) = 0;
	virtual long GetUdxBuffCount() = 0;
	virtual void GetAddrString(char* addrbuff,SOCKADDR* pAddr) = 0;
	virtual void SetThreadName(char* szName) = 0;
	virtual void GetHostIPAddr(char* szHost,unsigned short port,SOCKADDR*pDesAddr,BOOL bSelfIPv4 ) = 0;
	virtual void GetBuildString(char* strVer,int len) = 0;
	virtual void UdxSleep(int ms ) = 0;
	virtual void DBGStr(const char* szInfo,...) = 0;
	virtual char* GetBuffSizeInfo() = 0; 

};

#define UDXCHANNELCOUNT	2	//通道个数
#define MSGID	0			//消息通道
#define DATAID	1			//数据通道
#define UDXFILECHANNEL		65530//UDX文件通道

class IUdxInfo									//udx当前信息，大部分信息为主通道信息
{
public:
	UINT64 m_dwRead;								//当前接收到的数据长度,已经确认的
	UINT64 m_dwWrite;							//当前接发送的数据长度,已经确认的
	UINT64 m_ReadCount;							//当前接收到的包数,已经确认的
	UINT64 m_WriteCount;							//当前接发送的包数,已经确认的
	UINT64 m_SendTotalCount;						//总共发送的总包数,包括重传部分
	UINT64 m_ReSendCount;						//快速重传的包数

	UINT64 m_dwDumpCount;						//收到的重包数
	UINT64 m_dwOutRange;							//收到的,不在接收窗口中的包数
	DWORD m_start;						//当前起始时间
	DWORD s1,e1,s2,e2;				//当前收发的起始序号,及当前缓冲区中的待发最大送序号
	DWORD m_ackcount;							//收到的ACK数量
	UINT64 m_currentspeedread;					//当前接收速度,字节/秒,已经确认的
	UINT64 m_currentspeedsend;					//当前发送速度,字节/秒,已经确认的
	DWORD m_lastUpdateTime;						//上次更新udxinfo时间 
	
	int m_ttl;									//当前的往返时间
	int m_minttl;								//此链轮的最小RTT
	int m_SecSendSize;							//每个统计周期发送的长度
	int m_SecReSendSize;						//每个统计周期重发的数据长度
	
	UINT64 m_SendBewControl;						//每个统计周期发送的速度
	UINT64 m_WillBeSendSize;						//即使需要发送的速度（量）

	int m_sendsyncount;							//发送的同步包个数
	int m_readsyncount;							//接收到的同步包个数

	UINT64 m_SendBuffCount[UDXCHANNELCOUNT];		//成功调用sendbuff的次数
	UINT64 m_WriteBuffCount[UDXCHANNELCOUNT];	//发送成功的次数
	UINT64 m_ReadBuffCount[UDXCHANNELCOUNT];		//接收到由sendbuff产生的接收包次数
	UINT64 m_FecDecoderCount;		//接收到由sendbuff产生的接收包次数
	
	virtual void GetSpeedStr(char * buff,BOOL bSend = TRUE,BOOL bCurrent = FALSE)=0;			//得到实时/平均速度,字符串
	virtual DWORD GetCurrentSpeed(BOOL bSend = TRUE)=0;											//得到当前速度
	virtual DWORD GetSpeed(BOOL bSend = TRUE)=0;												//得到平均速度
	virtual char* GetInfo()=0;																	//得到字符串,当前调试信息
	virtual void Reset()=0;																		//重新计时
	virtual void UpDateCurrentSpeed()=0;														//刷新各种信息
	virtual float GetLostRate() = 0;															//当前丢包率
	virtual float GetLostRate2() = 0;															//实时丢包率
	virtual DWORD GetLastAvgSendSpeed() = 0;
	
	//备注:
	//当联接建立以后,各种参数都会自动在UDX内部去不断更新,但是,应用层也可以调用reset进行重新计算,但是调用RESET不会引
	//响UDX内部传输,只是作为应用层的参考数据.
}UDXPACKED;


struct IUdxCfg									//单个UDX的一些设置
{
	int mode;//设置Fastudx.cfg.mode=1;表示，所有生成的UDXTCP必须CPY 全局配置，否则按默认配置来
	int maxdatabuffwnd[2];						// 
	int submss;									//当前连接的MSS,目前默认为1024，最大为1400，一般情况下不需要更改这个值。
	int maxlostrate;							//最大丢包率，default(350/1000==35% ),输入范围(1~1000)
	int expectbew;					//固定流量（B/秒），
	int maxsendbew;					//单条连接最大流量(B/S)
	int minsendbew;					//设置最低流量
	int gamemode;			//最大乱序距离
	int mergeframe;					//是否合并包发送

	int brokenframe;				//废弃

	int fastmode;					//废弃
	int fixbew;						//废弃
	int filetransmode;				//废弃
	int delaytosend;				//废弃

	int segmentspace;				//失序间隔，默认是3，（1~5)，不建议修改

	int maxackcheckcount;			//废弃

	int maxlocalackchecktime;		//废弃
	int maxremoteackchecktime;		//废弃
}UDXPACKED;

class IUdxLogSink
{
public: 
	virtual void Log(char* str) = 0;//内部UDX写日志的信息，通过这个接口导出，应用层可以从这个派生后，写LOG
};

struct IUdxGlobalCfg
{
	IUdxGlobalCfg(){memset(this,0,sizeof(IUdxGlobalCfg));};
	int bInit;
	int mastver;
	int slaverver;
	int udxclock;				//udx内部时钟,默认25ms
	int udxcheckacktimer;		//udx内部ack回包频率,默认35毫秒
	int udxmintimeout;			//udx内部最小超时300ms
	int udxmaxtimeout;			//udx内部最大超时10000ms
	int udxackcount;			//多少个包回应一个ACK
	int udxdebug;
	DWORD maxsendbew;			//全局上传流量，包括丢的包
	char ext[10];
	IUdxLogSink* pLog;
	int bDisableLog;
}UDXPACKED;

struct UdxConnectInfo
{
	UDP_SHORT linktype;
	UDP_SHORT linkindex;
	UDP_BYTE rtpmode:1;
	UDP_BYTE linksubtype:7;
	UDP_BYTE internaltype:4;//内部链接类型
	UDP_BYTE notused1:4;
	UDP_BYTE notused2;
	UDP_BYTE notused3;
	UDP_BYTE notused4;
}UDXPACKED;

#define P2PIDLEN	64

class IUdxBuff;
struct Udx_P2p_TS_Info
{
	int bCaller;
	UDP_SHORT p2pindex;
	UdxConnectInfo coninfo;
	char self[P2PIDLEN];			//用户1,一般是自己的用户ID别名
	char des[P2PIDLEN];			//用户2，对方的ID,别名
	char dessn[P2PIDLEN];			//用户2，对方的ID
	char p2pserver[P2PIDLEN];		//p2p服务器IP
	char tsserver[P2PIDLEN];		//TS服务器IP
	UDP_SHORT p2pport;		//
	UDP_SHORT tsport;		//
	UINT64 userdata;
	IUdxBuff* pInitData;
}UDXPACKED;

class IUdxUnkownPackSink		//对于非UDX的UDP包，设置了这个回调后，会回调出这些包，给应用处理
{
public:
	virtual void OnUnkownData(SOCKADDR * pAddr,BYTE* pData,long len){};
	virtual void OnThreadExit(){};
};

class IWaitEvent				//UDX通道是否可发数据事件通知
{
public:
	virtual int Wait(DWORD ms) = 0;
	virtual void SetEvent() = 0;
};


struct FileInfo
{
	UINT64 len;
	char name[256];
	UDP_BYTE context1[10];
	UDP_BYTE context2[10];
	UDP_BYTE context3[10];
}UDXPACKED;


class IUdxFSink					//文件回调接口
{
public:
	//当远端有新的文件到来请求事件
	virtual int OnNewFile(FileInfo * pInfo,BOOL bContinued){return bContinued + 1;};//1 save new file ,2 continue,else cancel
	//经接收方同意，传输开始，针对传输方
	virtual void OnTransFileBegin(char* filename,BOOL bSend){} ;
	//远程取消息发送
	virtual int OnRemoteCancel(BOOL bSend){return 0;} ;
	//文件传输完成
	virtual void FileTransmitDone(char* filename,BOOL bSend){};//bSend = false时为接收，当收到此事件时，表示文件接收完成，不能立即删除掉UDX对象，
	//当接收完成时，会发送一个通知到发送方，触发发送方的FileTransmitDone消息。过早的关掉接收UDX,会导致发送方收不到发送完成事件。
	//所以，在文件传输时，应该当发送方收到FileTransmitDone事件时再关掉UDX，可以保证两边都可以收到此事件，并保证文件传输完整
	
	//文件数据发送续传点
	virtual void OnFileContinue(UINT64 startpoint,BOOL bSend){};
	//文件数据到来多少
	virtual void OnFileReadByts(DWORD dwSize){};
	//文件数据发送多少
	virtual void OnFileWriteByts(DWORD dwSize){};
	
	//提供加密接口
	virtual void OnEncoderByts(BYTE*pData,DWORD dwSize){};
	
	//提供解密接口
	virtual void OnDecoderByts(BYTE*pData,DWORD dwSize){};

	//接收方有一个同名文件，返回1，表示强制重新传 ，其他表示正常流程, 如果正常流程就会取，前10，后10，中间10个字节（三点采样）比对，发现相同就认为是同一个文件，立即返回成功
	virtual int OnCheckSameNameFile(FileInfo * pInfo){return 0;} ;

	//出错信息
	virtual void OnTransFileErro(int errocode){};

	virtual void OnCancelAck(BOOL bSend){};//表示对方成功应答你的cancel请求

	virtual int OnReciveNewFileRequest(FileInfo * pInfo){return 1;};//返回非1，表示拒绝
};

class IUdxFileTransmitor 
{
public:
	//得到当前收发了百分比
	virtual float GetPercent(BOOL bSend) = 0;
	//发送文件
	virtual int SendFile(char* filename) = 0;
	//发送是否完成,当接收方返回真时，不能立即删除UDX对象，保证在发送方进行删除UDX对象
	virtual BOOL IsDone(BOOL bSend) = 0;
	//设置保存文件路径目录
	virtual void SetSaveFileDir(char* savedir) = 0;
	//设置保存文件名，绝对路径
	virtual void SetSaveFileName(char* savename) = 0;
	//设置文件事件回调接口
	virtual void SetFSink(IUdxFSink * pSink) = 0;
	//取消发送或接收取消发送或接收
	virtual void Cancel(BOOL bSend) = 0;
	//得到打开的文件的总长度
	virtual UINT64 GetTotalSendfileLen() = 0;
	virtual UINT64 GetTotalReadfileLen() = 0;	
	//设置断点间隔长度，如果lBp小于1024字节，结果相当于lBp = 1024.默认是2M，每2M数据写一次文件
	virtual void SetBPLength(long lBp) = 0;
	//得到BP长度
	virtual long GetBPLength() = 0;

	virtual char * GetSendFileName() = 0;
	virtual char * GetReadFileName() = 0;
	//发送文件
	virtual void SendFileW(wchar_t* filename) = 0;
	virtual void ResetState(BOOL bSend) = 0;//重置收发状态，bsend = true发送，反之接收状态
};

class IUdxBuff;
class IUdxP2pSink
{
public:
	virtual void OnP2pFaild(char* user1,char*user2,INT64 userdata,UdxConnectInfo* pInfo ,IUdxBuff* pInitData){};
};
class IUdxP2pClient//P2P发起接口
{
public:
	virtual void SetNatServer(char*mip,long mport) = 0;
	virtual void SetTimeOut(DWORD ms = 30000) = 0;
	virtual BOOL ConnectServer(char* user1,char* user2,char* szDesID,UINT64 dwUser,BOOL bCaller,UdxConnectInfo* pInfo = NULL,IUdxBuff* pInitData = NULL) = 0;
	virtual void Start() = 0;
	virtual void Stop() = 0;
	virtual void Clear() = 0;//立即清除之前联接的用户，也可以不管，内部会定时清理
	virtual void SetSink(IUdxP2pSink*pSink) = 0;//设置P2P 回调指针
	virtual void SetCB(LPUDXP2PPRC pCB) = 0;//设置P2P 回调指针
};
#define UdxTrackData_Len	30

struct UdxTrackData
{
	UINT64 data[UdxTrackData_Len];
};


struct UdxFifoArrayBuffHead
{
	UDP_SHORT cmd;
	UDP_SHORT buffcount;
}UDXPACKED;


#ifdef WIN32
#pragma pack( pop)
#endif


class IMediaPush
{
public:
	//UDX侦模式发送音视频及其他数据
	//每一侦数据，被 UDX增加了一个UdxFrameType包头，用于丢侦处理，及包的识别
	virtual void SendFrame(UDP_LONG sid,UDP_SHORT sbid,BYTE* pData,int len,int type1/* AUDIOFRAME_A or VIDEOFRAME_I or VIDEOFRAME_P */,int type2,UDP_BYTE JumpFrameMode = 0) = 0;

	//智诺专有发送接口
	virtual void ZNSendFrame(UDP_LONG sid,UDP_SHORT sbid,Media_FrameInfo &FrameInfo) = 0;

	//设置最大缓冲的音频侦数，当网络拥塞时，能缓存的最大包数，用户可以根据，采集的周期 * maxcount = 最大语音延迟时间
	virtual void SetAudioFrameMaxCount(int maxcount) = 0;

	virtual void SendFrames() = 0;
	//清除缓存的音视频数据，恢复到初使状态，此时可以强制编码器产生I侦，进行缓冲音视频
	virtual void Reset() = 0;

	//只传音频
	virtual void OnlyAuido(BOOL bOnly) = 0;

};

class IUdxBuff;
class IUdxFifoList;
class IFastUdx;
class IUdxTcp : public IUdxFileTransmitor //==============    UDX 单条连接接口       ====================
{
public:
	virtual long AddLife() = 0;							//给连接增加引用计数，类是于com组件，这样可以在第二个线程中操作这个联接，而不会出问题
	virtual void Destroy() = 0;							//断开这个联接
	virtual BOOL IsConnected() = 0;						//联接是否正常
	virtual BOOL IsFullBuffs(int ich) = 0;				//现在是否不能发数据了
	virtual void SetBuffWindow(int ich,DWORD size) = 0;	//设置缓冲大小，以字节为单位,缓冲长度受用户设置SetFloatDataSize的限制
	virtual void SetMaxDataWindowSize(int ich,DWORD size) = 0;//设置窗口大小，以包数量为单位，可以为（8，16，32，64，...，8092~ 最大（8092*2））
	virtual BOOL SendBuff(BYTE* pData,int len) = 0;		//返回真，表示，指定数据已经成功拷贝到UDX缓冲中，缓冲长度受用户设置SetFloatDataSize的限制
	virtual BOOL SendMsg(BYTE* pData,int len) = 0;		//发送消息
	virtual IUdxInfo* GetUdxInfo() = 0;					//得到内部一些信息
	virtual IUdxCfg * GetUdxCfg() = 0;					//单个联接的，相关配置项
	virtual UDP_SHORT GetStreamID() = 0;				//本地的流ID
	virtual UDP_SHORT GetDesStreamID() = 0;				//联接成功后，远端的本地流ID
	virtual UINT64 GetUserData() = 0;					//与联接关联的用户数据，可以是指针，自定义数据
	virtual void SetUserData(UINT64 dwUser) = 0; 
	virtual IWaitEvent* GetWaitEvent() = 0;				//联接，当有数据可发时的事件对象指针
	virtual SOCKADDR *GetRemoteAddr() = 0;				//联接成功后，远端的IP信息
	virtual int __DSendUdxBuff(SOCKADDR * pAddr ,BYTE* pData,int len) = 0;		//可以用来发送非可靠数据
	virtual int __DSendUdxBuff2(SOCKADDR * pAddr ,BYTE* pData,int len) = 0;		//可以用来发送非可靠数据，但是会触发
	virtual DWORD GetAppBuffAndUdxWndBuffSize() = 0;	//得到当前拷贝到udx内部的数据长度，这些数据还没有得到确认
	virtual DWORD GetBuffWindow(int ich) = 0;			//得到应用层设置的缓冲长度，这个不包括内部窗口中含有的长度
	virtual void SetTimeOut(int con,int hardbeat,int contimeout) = 0;		//设置超时，分别为，联接超时，心跳间隔，和联接保活时间，秒为单位
	virtual UdxTrackData* GetUdxTrackData() = 0;		//得到额外的与联接共享的内存关联
	virtual void SetFloatDataSize(int floatdatasize) = 0;		//设置应用缓冲长度,如果没有设置时为0，就例用默认缓冲长度8M，只针对数据通道有效，消息通道不受这个影响
	virtual INT64 GetFloatDataSize() = 0;					//得到之前设置的缓冲大小。
	virtual void Close(BOOL bDelay = FALSE) = 0;		//如果联接是正常的情况下，会断开联接，否则，什么也不会做,如果bdely = true,则会将还没有发送完成的数据，发送完成后关闭联接，否则立即关闭
	virtual long ReleaseLife() = 0;						//减少由addlife引起的计数
	virtual void DetectReadedBuffSize(long &readsize,long &writesize) = 0;	//用户检测，在没有触发OnStreamwrite/OnStreamRead事件中的未处理的数据长度
	virtual IMediaPush* GetMediaPush() = 0;				//得到push接口,用此接口来推送音视频侦，可以防止花屏
	virtual IFastUdx * GetFastUdx() = 0;				//返回IFastUdx对象
	virtual BOOL IsTransLink() = 0;//返回是否为中转联接
	virtual SOCKADDR *GetTransServerAddr() = 0;//返回当前走的中转IP
	virtual UDP_SHORT GetLinkType() = 0;//联接时，通过UdxConnectInfo指定的联接类型1
	virtual UDP_SHORT GetLinkIndex() = 0;//联接时，通过UdxConnectInfo指定的联接类型2
	virtual void EnableAutoAjustBuffs(BOOL bEnable) = 0;//让UDX自动调整发送缓冲长度
	virtual int GetLinkDirect() = 0;//1，表示联接发起方，0表示联接接受方
	virtual int GetUncheckedUserBuffCount(int ich) = 0;//返回多少个应用层包还没有被返回
	virtual BOOL PutLocalConnectInitData(BYTE* pData,int len) = 0;//设置本地联接初始化数据
	virtual IUdxBuff* GetRemoteConnectInitData() = 0;//得到远程初始化联接数据
	virtual IUdxBuff* GetLocalConnectInitData() = 0;//得到本地联接初始化数据
	virtual BOOL IsAllSendDone(int ch) = 0;//所有的数据是否发送完
	virtual void ClearNoSendData() = 0;//清除目前还没有被发送的数据，从UDX缓冲队列中
	virtual void PostEvent(IUdxTcp *pDesTcp,int iEvent,UINT64 userdata) = 0;//给其他联接发送一个自己定义执行回调，pDesTcp会回调OnPostEvent(IUdxTcp*src,int iEvent,UINT64 userdata);
	virtual void ChangeToRTPMode(BOOL bRTP,int sizemode) = 0;//把消息通道改成FEC + RTP模式，用来传输音频，允许丢包，减少延迟,sizemode 0~6
	virtual BOOL SendBuffList(IUdxFifoList * pList) = 0;//sendbuff的另外接口，支持连续的碎片数据，组成完整侦发送
	virtual BOOL SendMsgList(IUdxFifoList * pList) = 0;		//sendmsg的另外接口，支持连续的碎片数据，组成完整侦发送
	virtual int WaitForClose() = 0;//此函数为柱塞调用，返回1表示，对方成功收到断开消息，其他表示超时返回,超时时间目前为最大500ms.
	virtual void DisableFec() = 0;//禁止FEC
	virtual int BlockSendBuff(int ich,BYTE* pData,int len,int ms = 0) = 0;//阻塞发送，直到发送完成，返回发送长度，或者返回-1，表示联接断开
	virtual int SetFecParam(int ch,int k,int n) = 0;//设置FEC参数九
	virtual long GetSpendTime(BOOL bSend) = 0;//返回收发文件花费的时间，毫秒数，兼容接口
	virtual BOOL LinkCpyRoute(IUdxTcp* pTcp,BOOL bAndClose) = 0;//从中转切到直联模式，此接口必须要本地存在，一个TS链接和直联对象
	virtual UDP_SHORT P2pConnectTo(char* szDesSN,UdxConnectInfo * pConnectInfo) = 0;
	virtual SOCKADDR *Get3wAddr() = 0;
	virtual void DownloadP2pUserLists() = 0;
	virtual UDP_BYTE GetLinkSubType() = 0;//获取子联接信息
	virtual UDP_BYTE GetLinkInternalType() = 0;//获取子联接信息
	virtual int GetLinkLost() = 0;
};

//=======================================容易混淆的几个方法==========================================//

//Close();//断开连接，并不计数变化，对方触发broken,本地不会触发broken
//
//WaitForClose();//断开连接，并计数变化--，对方触发broken,本地不会触发broken，等待1.5秒，对方立即响应broken
//
//Destroy();//断开连接，并计数变化--，对方触发broken,本地不会触发broken
//
//ReleaseLife();//数变化--

//技巧：
// Q1 单线程中，如果我创建了TCP对象，调用了AddLife
// 关闭时 我可以
// 1.1 先调用ReleaseLife然后调用Close；
// 1.2 先调用Close 然后调用ReleaseLife
// 1.3 直接调用Destory。
// 
// Q2 单线程中如果 我创建了TCP对象，没有调用AddLife
// 关闭时 调用destroy,不必严格遵守计数，因为没有第二个线程访问同一个对象。
// 
// Q3 多线程中，如果想复制TCP，那么有几个线程，就要调用
// 几次AddLife，根据需要，不同的线程退出前可以调用Destory ，
// 或者ReleaseLife，但是必须有一个线程最终要调用关闭即：
//    Destory 或者 Close + ReleaseLife

//==================================================================================================//

class IMultCardTcp;
class IUdxTcpSink
{
public:
	virtual void OnStreamPreConnect(SOCKADDR *pAddr,IUdxTcp * pTcp,UdxConnectInfo * pInfo){};	//被动方会回调此接口，可设置各项参数
	virtual void OnStreamConnect(IUdxTcp * pTcp,int erro){};
	virtual void OnFileStreamConnect(IUdxTcp * pTcp,int erro){};				//0,表示新的一个文件传输会话，1，会话结束
	virtual void OnStreamBroken(IUdxTcp * pTcp){};								//断开
	virtual void OnStreamRead(IUdxTcp * pTcp,BYTE* pData,int len){};			//读
	virtual void OnStreamWrite(IUdxTcp * pTcp,BYTE* pData,int len){};			//写
	virtual void OnStreamMsgRead(IUdxTcp * pTcp,BYTE* pData,int len){};			//消息的读
	virtual void OnStreamMsgWrite(IUdxTcp * pTcp,BYTE* pData,int len){};		//消息的写
	virtual void OnStreamNeedMoreData(IUdxTcp *pTcp,int needdata){};			//可以发起新的写,有needdata(<=0表示可以发数据)字节空间供发送，这个一般在有数据被确认后触发
																				//这个回调有点类是于完成通知，这个只是有包被确认的通知，所以，应用层可以利用他不断的进行投递
																				//而不需要启动额外的线程来完成发送。第一次sendbuff后可以通过这个事件不断叠加发送。
	virtual void OnUnSafeStreamRead(IUdxTcp * pTcp,BYTE*pData,int len){};		//本条连接的不可靠数据，一般用来发送音频

	virtual void OnInteranlThreadExit(){};										//内部线程释放时，处理，这一般给象java,com,jni中使用，用于释放资源
	virtual void OnStreamLinkIdle(IUdxTcp *pTcp){};								//当链路保持，但是应用层没有数据可发时，
																				//会收到心跳，这是心条的处理函授，用户可以打印调试信息，证明联接正常。一般不处理。
	virtual void OnStreamChancetoFillBuff(IUdxTcp *pTcp){};						//UDX内部发送线程提供机会，进行发送数据,每50MS固定频率调用
	virtual void OnMediaPushFrameEvent(IUdxTcp * pTcp,UDP_LONG sid,UDP_SHORT sbid,int type,int framecount){};		//sid,当发送时，出现SendFrame失败时，回调并报告实际缓冲了多少侦数据,0语音包1视频侦
	virtual void OnStreamFinalRelease(IUdxTcp * pTcp){};						//每个联接最后释放事件，可以处理一些关联的对象释放,不同于OnStreamBroken指收到断开消息就会调用，broken事件我们可以(清除掉所有引用releaselife)，使其进入OnStreamFinalRelease
																				//程序内部可能还有可能其他保存了对应的联接的信息，如果做过早的释放动作会引起不同步的问题
																				//这个回调是每一个联接的最后释放入口，当所有应用层对这个联接没有了引用保护(addlife)
	virtual void OnMultCardConnect(IMultCardTcp * pTcp,int erro,int linkcount){};
	virtual void OnMultCardBroken(IMultCardTcp * pTcp){};
	virtual void OnMultCardRead(IMultCardTcp * pTcp,BYTE*pData,int len){};
	virtual void OnLocalFastDestroy(IUdxTcp* pTcp){};//当本地销毁IFastUdx接口对象是回调给应用层，以释放对应资源
	virtual void OnFastUdxTimer(){};//udx定时器，每50MS触发，本定时器不能被柱塞
	virtual void OnPostEvent(IUdxTcp*src,int iEvent,UINT64 userdata){};//由src发起的事件，产生的回调，userdata,为自己定指针由用自己扩展
	virtual void OnFastUdxFinalRelease(){};//当IFastUdx->Destory()结束的地方会调用,可用于回调释放sink宿主
	virtual void OnTcpCongestionNotify(IUdxTcp* pTcp,float lost,UINT64 speed,int rtt){};
	virtual char*OnGetServerP2pConfigJson(IUdxTcp* pSrc,IUdxTcp* pDes){return "{}";};//pSrc请求P2P给pDes;
	virtual void OnP2pTsFaild(BYTE bCaller,UDP_SHORT p2pindex,char * szDesSn,UINT64 userdata,char* szErroMsg = NULL){};//P2PTS请求失败
	virtual void OnLoopClientsEvent(int type,IUdxBuff* pData,IUdxTcp* pTcp){};//响应服务轮询事件
	virtual void OnTsLinkChange(IUdxTcp* pTsTcp){};//TS链接转换成P2P链接
};

class IUdxTrans
{
public:
	virtual void OpenChannel(char*ip,UDP_SHORT port,char* strCName) = 0;		//打开一个中转通道,通道名不得等于或超过25个字符
	virtual void CloseChannel(char*ip,UDP_SHORT port,char* strCName) = 0;		//关掉一个中转通道
	virtual void CloseAllChannels() = 0;										//关掉所有的中转通道
};

struct IFastPreCfg : public IUdxCfg//在生成一个IUdxTcp接口时，预先设置的一些固有属性 
{
};
class IUdxUdpIOBase
{
public:
	virtual int InputUdxData( struct sockaddr * from,BYTE* pData,int len) = 0;//填充from和data,len为pData提供的缓冲长度
	virtual int OutputUdxData( struct sockaddr * adrrto,BYTE* pData,int len) = 0;//发送地址及待发送的数据及长度
};


class IFastUdx//UDX实例对象 ==============    UDX 核心对象       ====================
{
public:
	virtual BOOL Create(char* ip = NULL,UDP_SHORT port = 0) = 0;//创建udp socket,对指定端口绑定完成监听
	virtual BOOL AddBinding(char* ip,UDP_SHORT port,char* szName) = 0;//添加多IP时其他IP的绑定完成监听
	virtual IUdxTcp* Connect(char* ip,UDP_SHORT port,BOOL bSync = FALSE,
		UINT64 dwUser = 0,UINT64 expectbew = 0,int lostrat = 100,
		char*strCName = NULL,UdxConnectInfo*pInfo = NULL,IUdxBuff * pInitBuff = NULL,BOOL bAutoRelase = TRUE) = 0;


	virtual	BOOL Attach(SOCKET s) = 0;											//绑定已经存在的UDP套接字
	virtual int Dettach() = 0;
	virtual void Destroy() = 0;													//销毁UDX对象
	virtual void SetUnkownPackSink(IUdxUnkownPackSink *pUnkownPackSink) = 0;	//设置非UDX的UDP包时回调类
	virtual void SetSink(IUdxTcpSink * pSink) = 0;								//设置基本事件的回调类
	virtual void SetServerBlockSize(int size) = 0;								//设置内部数据流程处理线程数，比如，逻辑处理数，事件回调线程数，IO线程数等
	virtual SOCKADDR *GetLocalAddr() = 0;										//得到默认UDX端口占用信息
	virtual IUdxP2pClient* GetP2pClient() = 0;									//得到P2P接口
	virtual int __DSendUdxBuff(SOCKADDR * pAddr ,BYTE* pData,int len) = 0;		//直接向指定的地址发送非UDX 原始数据包
	virtual IUdxTrans* GetUdxTrans() = 0;										//得到中转传输接口
	virtual void SetUdpSendThreadCount(int count) = 0;							//设置内部发送UDP的线程数
	virtual void SetConnectTimeOut(int secs) = 0;								//设置新联接超时
	virtual void Enable_DONTFRAGMENT(BOOL bEanble) = 0;							//设置底层UDP是否允许分片，默认不允许分片
	virtual int GetClientsCount() = 0;											//返回总的联接数，但这个联接数不是实时的
	virtual IUdxCfg* GetPreCreateUdxTcpCfg() = 0;								//设置一个预先配置的配置
	virtual void SetCB(LPUDXPRC pcb) = 0;
	virtual IMultCardTcp* CreateMultCard() = 0;
	virtual void SetUserData(UINT64 dwUser) = 0;									//设置关联的用户数据指针
	virtual UINT64 GetUserData() = 0;											//获取关联的用户数据指针
	virtual void LowCost() = 0;													//取小开销，这个会减少udx的线程开销，数量
	virtual int GetTransFromLinkCount() = 0;									//获取转发的路数
	virtual void EnableTimer(BOOL bEnable) = 0;									//打开或关闭udx的主对象的timer
	virtual void SendUdpBrocast(BYTE* pData,int len) = 0;
	virtual void EnableUdpBrocast() = 0;
	virtual void SetDeviceID(char* szID) = 0;
	virtual void ThreadDestroy() = 0;											//开辟线程进行关闭，等功能等同Destroy,但是在这个过程中不能删除sink,当关闭完成后会回调sink->OnFastUdxFinalRelease，在这里可以安全释放回调对象sink
	virtual void SetSocketWindowSize(int wtype,int wsize) = 0;					//wtype = 0 SENDBUFF, = 1 RECVBUFF,最小值为1MB(1024*1024)字节
	virtual void SetSameSrcMaxCount(int maxcount = 5) = 0;						//设置源地址相同的联接最大数
	virtual void EnableWriteEvent(BOOL bEnable) = 0;							//	允许写事件回调，用于节约CPU,内存
	virtual BOOL IsIPv6() = 0;													//本地默认占用地址是不是ipv6
	virtual BOOL LinkCpyRoute(UDP_SHORT src,UDP_SHORT des,BOOL bAndClose) = 0;	//地址切换
	virtual void P2PMsgConnectRegister(char* szMsgServerIP,UDP_SHORT port,Udx_P2p_TS_Info* pP2pInfo) = 0;//注册P2P用户
	virtual void PostP2pRequest(Udx_P2p_TS_Info* pP2pInfo) = 0;					//发起P2P请求
	virtual void SetUdxUdpBase(IUdxUdpIOBase* pIOBase) = 0;						//设置UDP收发原始接口
	virtual void PostLoopClientsEvent(int type,IUdxBuff* pData) = 0;			//投递一个客户端轮询
	virtual void EnableFileTransmit(BOOL bEnable) = 0;
};

class IMultCardTcp																//多个网卡 绑定后，同时为一个流发送数据，或接收数据
{
public:
	virtual BOOL AddBinding(int index,char* ip,UDP_SHORT port) = 0;				//增加绑定IP
	virtual BOOL Connect(char*ip,UDP_SHORT port) = 0;							//通过指定的IP联接对方IP + port
	virtual void Close() = 0;													//断开联接
	virtual void Destroy() = 0;
	virtual BOOL SendBuff(BYTE* pData,int len) = 0;
	virtual void AddLife() = 0;
	virtual BOOL IsConnected() = 0;
	virtual UDP_SHORT GetMultCardID() = 0;										//取得本联接的ID号
	virtual UDP_SHORT GetMultCardDesID() = 0;									//取得本联接的对端ID号
	virtual void SetFloatSize(int floatsize ) = 0;								//设置缓冲数
	virtual BOOL GetRemoteIPList(char* pList,int &bufflen) = 0;					//（IP:PORT空格） ip-port,返回false表示缓冲不够，bufflen返回需要的长度,空格分隔
	virtual BOOL GetLocalIPList(char* pList,int &bufflen) = 0;					// ip-port,返回false表示缓冲不够，bufflen返回需要的长度,空格分隔
	virtual IWaitEvent* GetWaitEvent() = 0;
};


class IUdxLock//为了在编写跨平台代码时，提供的锁
{
public:
	virtual void Lock() = 0;
	virtual void Unlock() = 0;
	virtual void Destroy() = 0;
};

class IUdxEvent //事件对象
{
public:
	virtual void SetEvent() = 0;
	virtual void ResetEvent() = 0;
	virtual BOOL Wait(int ms) = 0;
	virtual void Destroy() = 0;
};

class IUdxBuff
{
public:
	virtual BYTE* ChangePoint(int iLen,int bNoCpy = 1) = 0;									//动态扩展缓存大小
	virtual BYTE* GetBuff(void) = 0;											//取得缓存头指针
	virtual int GetLen(void) = 0;												//返回缓冲实际使用长度
	virtual int GetMaxLen(void) = 0;											//返回最大缓冲长度
	virtual void Zero() = 0;													//清0
	virtual void FreeMem() = 0;													//释放内存
	virtual void Reset() = 0;													//清0，并把使用长度清0
	virtual void Bind(BYTE* pData,int len) = 0;									//把指定内存copy到本对象中
	virtual BOOL Pop(BYTE* pData,int len) = 0;									//从指针头，弹出指定长度len的数据，内容cpy到pData中，原缓冲长度会减少len
	virtual void AppendBuff(BYTE* pData,int iLen) = 0;							//追加一部分缓冲
	virtual void Destroy() = 0;													//释放自己
	virtual long AddLife() = 0;													//增加引用计数
	virtual BOOL Drop(int left,int len) = 0;									//丢掉部分数据，是左边还是右边
	virtual void ExternDataPoint(BYTE* pData,int len) = 0;								//设置扩展数据
	virtual void GetExternDataPoint(BYTE* pData,int len) = 0;								//获取扩展数据指针
	virtual void InitPtSize(int size) = 0;
	virtual void SetPoint(int index,void* pt) = 0;
	virtual void* GetPoint(int index) = 0;
	virtual SOCKADDR* GetFrom() = 0;

};

class IUdxFifoList //先进先出队列
{
public:
	virtual void AddBuff(IUdxBuff * pBuff) = 0;									//增加一个buff对象
	virtual IUdxBuff* GetBuff() = 0;											//取出一个完整的ibuff对象
	virtual void AddBuff(BYTE * pData,int len) = 0;								//压入指定的长度数据
	virtual void GetBuff(BYTE** ppData,int &len) = 0;							//取出当前一个完整的buff数据
	virtual void FreeBuff(BYTE* pData) = 0;										//释放由getbuff(BYTE**)指针所指内存
	virtual int GetBuff(BYTE* pData,int len) = 0;								//给定指度长度的缓冲填充pData,原数据批针后移len
	virtual int GetBuffSize() = 0;												//返回list中的数据总长度
	virtual int GetBuffCount() = 0;												//返回list中IBuff的数量
	virtual void EnableEvent(BOOL bEnable) = 0;									//是否与事件关联
	virtual BOOL Wait(int ms) = 0;												//当与事件关联后，当有新数据加入时，会触发事件
	virtual void Clear() = 0;													//清除所有数据
	virtual void Destroy() = 0;													//释放自己
	virtual void SetWaitEvent(IUdxEvent* pEvent) = 0;							//关接接收事件
	virtual IUdxBuff* BuildOneBuff(UDP_SHORT cmd) = 0;								//生成一个大包
	virtual void Attach(BYTE* pData,int len) = 0;								//解析包
	virtual UDP_SHORT GetCMD() = 0;
	virtual IUdxBuff* TryGetBuff() = 0;											//尝试取出一个完整的ibuff对象
	virtual void PopFrontBuff() = 0;											//释放第一个buff
	virtual void PopFrontBuffCount(int count) = 0;								//释放队前N个BUFF
};

class IUdxTime //时间tick
{
public:
	virtual UUDX_LONG GetTickCount() = 0;
	virtual void Destroy() = 0;
	virtual UINT64 GetUSTickCount() = 0;
};

class IUdxThreadSink //线程回调
{
public:
	virtual void UdxRun(){};
	virtual void FreeSelf(){};
};
class IUdxThread  //线程接口
{
public:
	virtual void Start() = 0;
	virtual void Stop() = 0;
	virtual void SetCB(IUdxThreadSink* pSink) = 0;								//设置线程回调类，该类必须实现UdxRun()
	virtual void Destroy() = 0;
	virtual void StartEx(int stacksize,char* szThName) = 0;										//stacksize = 0等同start()
	virtual BOOL IsAllExit() = 0;
	virtual void AutoRelease(BOOL bAutoRelease) = 0;
};

class IUdxFile  //文件操作接口
{
public:
	virtual BOOL OpenFile(char* strFile,BOOL app = FALSE) = 0;					//打开已经存在的文件
	virtual BOOL OpenFileW(wchar_t* strFile,BOOL app = FALSE) = 0;
	virtual BOOL CreateFile(char* strFile) = 0;									//创建文件
	virtual void Close(void) = 0;												//关闭
	virtual BOOL IsOpen(void) = 0;												//文件是否已经被打开
	virtual UINT64 GetFileLength() = 0;											//返回文件长度
	virtual UINT64 Write(BYTE* pData, int len) = 0;								//写入文件
	virtual UINT64 Read(BYTE* pData, int len) = 0;								//读文件，返回真正读入的长度
	virtual void Seek(int type = 1) = 0;										//seek到头0，或尾1
	virtual void SeekTo(int type,UINT64 len) = 0;								//type 0从头开始，1尾开始 len，可为负值，是反方向
	virtual void Destroy() = 0;													//释放自己，并关掉文件
};

class IUdxRef
{
public:
	virtual long AddRef() = 0;
	virtual long Release() = 0;//负数调用会失败
	virtual long Add(long v) = 0;
};

class IUdxBuffPool
{
public:
	virtual IUdxBuff* GetNewBuff(int datalen) = 0;
	virtual void CheckPool() = 0;
	virtual void Destroy() = 0;												
};

class IUdxPing
{
public:
	virtual BOOL Ping(char* szHost,UDP_SHORT port) = 0;
	virtual void SetTimeOut(int ms) = 0;
	virtual BOOL IsIPV6() = 0;
	virtual void Destroy() = 0;
	virtual int GetPingValue() = 0;
};

//UDX 简易广播发布者
class IUdxSimpleBroacastServer
{
public:
	virtual BOOL Init(UDP_SHORT loalport,UDP_SHORT desport) = 0;//绑定本地端口
	virtual IUdxBuff* Recv(int ms = 10) = 0;//接收别人的广播，通过返回值->GetFrom得到地址
	virtual int Send(SOCKADDR * pDesAddr,BYTE* pData,int len) = 0;//单个回发消息
	virtual int Broacast(BYTE* pData,int len) = 0;//群发消息
	virtual int GetClientsCount() = 0;//返回可以广播的数量
	virtual int GetClientIPs(char* ips,int bufflen) = 0;//‘;'分隔IP
	virtual void Destroy() = 0;
};

//UDX广播接收者
class IUdxSimpleBroacastClient
{
public:
	virtual BOOL Init(UDP_SHORT loalport) = 0;//绑定本地端口
	virtual IUdxBuff* Recv(int ms = 10) = 0;//接收别人的广播
	virtual int Send(BYTE* pData,int len) = 0;//回发给广播者消息
	virtual void Destroy() = 0;
};




class UdxTcpPtHelper
{
public:
	UdxTcpPtHelper(IUdxTcp* pTcp){_pTcp = pTcp;};
	virtual ~UdxTcpPtHelper(){if(_pTcp)_pTcp->ReleaseLife();};
protected:
	IUdxTcp* _pTcp;
};
class UdxBuffPtHelper
{
public:
	UdxBuffPtHelper(IUdxBuff* pBuff){_pBuff = pBuff;};
	virtual ~UdxBuffPtHelper(){if(_pBuff)_pBuff->Destroy();};
protected:
	IUdxBuff* _pBuff;
};
class UdxListPtHelper
{
public:
	UdxListPtHelper(IUdxFifoList* pList){_pList = pList;};
	virtual ~UdxListPtHelper(){if(_pList)_pList->Destroy();};
protected:
	IUdxFifoList* _pList;
};
class UdxLockHelper
{
public:
	UdxLockHelper(IUdxLock* pLock){_pLock = pLock;pLock->Lock();};
	virtual ~UdxLockHelper(){_pLock->Unlock();};
protected:
	IUdxLock* _pLock;
};


IUdxGlobalCfg* GetUdxGlobalCfg();		//UDX的全局变量接口，用来设置一些全局参数
IFastUdx* CreateFastUdx();				//创建一个UDX对象
IUdxTools* GetUdxTools();				//UDX提供的辅助工具，一般用不到
IUdxLock* CreateUdxLock();				//创建一个锁
IUdxEvent* CreateUdxEvent();			//创建事件
IUdxFifoList* CreateUdxList();			//创建一个先进先出队列
IUdxTime* CreateUdxTime();				//创建tick对象
IUdxThread* CreateUdxThread();			//创建线程对象
IUdxFile* CreateUdxFile();				//创建udx文件对象
IUdxBuff * CreateUdxBuff(int size);		//创建一个buff对象
IUdxRef * CreateUdxRef();				//创建一个引用计数对象
IUdxBuffPool * CreateUdxBuffPool();		//创建一个buff池	
IUdxPing * CreateUdxPing();				//ping udx主机
IUdxSimpleBroacastServer * CreateUdxSimpleBroacastServer();	//广播服务			
IUdxSimpleBroacastClient * CreateUdxSimpleBroacastClient();	//广播客户端


#endif
