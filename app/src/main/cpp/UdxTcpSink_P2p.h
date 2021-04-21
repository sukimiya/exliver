#pragma once

#include <include/FastUdx.h>

class UdxTcpSink_P2p : public IUdxTcpSink {
public:
    UdxTcpSink_P2p();
    virtual ~UdxTcpSink_P2p();
public:
	void SendFrame(int bvideo,int bkey,BYTE* pData,int len);
	int P2pConnectDevice(char* szDeviceName);
private:
    IFastUdx* m_pFastUdx;
	IUdxTcp* m_pTcpData;
	IUdxTcp* m_pTcpRegister;
	BOOL m_bShutdown;
	IUdxLock * m_pLock;
	string m_name;
	string m_server;
	int m_port;

    void OnStreamRead(IUdxTcp* pTcp, BYTE* pData, int len);
    void OnStreamMsgRead(IUdxTcp* pTcp, BYTE* pData, int len);
    void OnStreamConnect(IUdxTcp* pTcp, int erro);
    void OnStreamBroken(IUdxTcp* pTcp);
    void OnMediaPushFrameEvent(IUdxTcp* pTcp, UDP_LONG sid, UDP_SHORT sbid, int type, int framecount);
	void OnFastUdxFinalRelease();
public:
    void initUdx();
    void unInitUdx();
    void Register(char* ip,int port,char*szName);
	void shutdownloop();
};
