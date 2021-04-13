#include "UdxTcpSink_P2p.h"

#define P2PSERVER_ADDR ("47.102.121.213")
#define P2PSERVER_PORT 8888

UdxTcpSink_P2p::UdxTcpSink_P2p() {
    m_pFastUdx = NULL;
	m_bShutdown = FALSE;
	m_pTcpData = NULL;
	m_pLock = CreateUdxLock();
}

UdxTcpSink_P2p::~UdxTcpSink_P2p()
{
	shutdownloop();
	if(m_pTcpData)
	{
		m_pTcpData->Destroy();
		m_pTcpData = NULL;
	}
	m_pLock->Destroy();
    unInitUdx();
}


void UdxTcpSink_P2p::SendFrame(int bvideo,int bkey,BYTE* pData,int len)
{
	UdxLockHelper cs(m_pLock);
	if(!m_pTcpData)
	{
		return;
	}
	if(bvideo)
	{
		if(bkey)
			m_pTcpData->GetMediaPush()->SendFrame(0,0,pData,len,VIDEOFRAME_I,0,0);
		else
			m_pTcpData->GetMediaPush()->SendFrame(0,0,pData,len,VIDEOFRAME_P,0,0);
	}else
	{
		m_pTcpData->GetMediaPush()->SendFrame(0,0,pData,len,AUDIOFRAME_A,0,0);
	}

}

void UdxTcpSink_P2p::OnStreamRead(IUdxTcp* pTcp, BYTE* pData, int len) {
    GetUdxTools()->DBGStr("==========================================\n");
    GetUdxTools()->DBGStr("get udx data\n");
    GetUdxTools()->DBGStr("==========================================\n");
}

void UdxTcpSink_P2p::OnStreamMsgRead(IUdxTcp* pTcp, BYTE* pData, int len) {
    OnStreamRead(pTcp, pData, len);
}

void UdxTcpSink_P2p::OnStreamConnect(IUdxTcp* pTcp, int erro) {
    if (pTcp->GetLinkType() == UDX_P2P_MSG_LINK_FLAG)
	{
        if (erro != 0)
		{ // 连接IDM失败
            GetUdxTools()->DBGStr("regist to idm server failed\n");
            this->unInitUdx(); // 再次发起连接
        } else 
		{ // 连接IDM成功
            GetUdxTools()->DBGStr("==========================================\n");
            GetUdxTools()->DBGStr("regist to idm server succeeded\n");
            GetUdxTools()->DBGStr("==========================================\n");
        }
    } else
	{
        if (erro != 0)
            return;

		m_pLock->Lock();

		if(m_pTcpData)
		{
			m_pTcpData->Destroy();
		}

		m_pTcpData = pTcp;
		pTcp->AddLife();

		m_pLock->Unlock();

        GetUdxTools()->DBGStr("udx client connect succeeded\n");
    }
}

void UdxTcpSink_P2p::OnStreamBroken(IUdxTcp* pTcp) {
    if (pTcp->GetLinkType() == UDX_P2P_MSG_LINK_FLAG) {
        GetUdxTools()->DBGStr("==========================================\n");
        GetUdxTools()->DBGStr("udx idm server disconnected\n");
        GetUdxTools()->DBGStr("==========================================\n");
        this->unInitUdx(); // 再次发起连接
    } else {
        GetUdxTools()->DBGStr("==========================================\n");
        GetUdxTools()->DBGStr("udx client disconnected\n");
        GetUdxTools()->DBGStr("==========================================\n");
    }
}

void UdxTcpSink_P2p::OnMediaPushFrameEvent(IUdxTcp* pTcp, UDP_LONG sid, UDP_SHORT sbid, int type, int framecount) {
    GetUdxTools()->DBGStr("OnMediaPushFrameEvent callback, type=%d,frameCount=%d\n", type, framecount);
}


void UdxTcpSink_P2p::OnFastUdxFinalRelease()
{
	if(m_bShutdown)
		return;
	initUdx();
	Register((char*)m_server.c_str(),m_port,(char*)m_name.c_str());
}

void UdxTcpSink_P2p::initUdx() {

	m_pFastUdx = CreateFastUdx();
	m_pFastUdx->SetSink(this);
	m_pFastUdx->Create(NULL, (UDP_SHORT)0);
}

void UdxTcpSink_P2p::unInitUdx() 
{
    if (m_pFastUdx != NULL) {
		IFastUdx* pTemp = m_pFastUdx;
		m_pFastUdx = NULL;
        pTemp->ThreadDestroy();
    }
}

void UdxTcpSink_P2p::Register(char* ip,int port,char*szName) 
{

	m_server = ip;
	m_port = port;
	m_name = szName;


    Udx_P2p_TS_Info info;
    memset(&info, 0, sizeof(info));

    strcpy(info.self,szName);
    strcpy(info.des, "");
    strcpy(info.dessn, "");

    strcpy(info.p2pserver, ip);
    info.p2pport = port;
    strcpy(info.tsserver, ip);
    info.tsport = port;

    GetUdxTools()->DBGStr("==========================================\n");
    GetUdxTools()->DBGStr("try to connect to idm server\n");
    GetUdxTools()->DBGStr("==========================================\n");

    m_pFastUdx->P2PMsgConnectRegister((char*)ip, port, &info);
}

void UdxTcpSink_P2p::shutdownloop()
{
	m_bShutdown = TRUE;
}
