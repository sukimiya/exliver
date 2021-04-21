//
// Created by sukim on 2021/4/7.
//

#include <jni.h>
#include "UdxTcpSink_P2p.h"
#include <rtmp.h>

UdxTcpSink_P2p udxTcpSinkP2P;
JavaVM *jvm;
CallJava *callJava;
/*
* Set some test stuff up.
*
* Returns the JNI version on success, -1 on failure.
*/
void byteLink(BYTE *string, BYTE *string1, BYTE *string2);

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    jint result = -1;
    jvm = vm;
    JNIEnv *jniEnv = NULL;

    if ((result = vm->GetEnv((void **) (&jniEnv), JNI_VERSION_1_4)) != JNI_OK) {
        return result;
    }
    return JNI_VERSION_1_4;
}

extern "C"
JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    jvm = NULL;
}
void byteLink(byte a[], byte b[], byte c[], byte d[], byte zzz[]) {
    int i;
    for (i = 0; i < 128; i++) {
        zzz[i] = a[i];
    }
    for (i = 128; i < 192; i++) {
        zzz[i] = b[i - 128];
    }
    for (i = 192; i < 224; i++) {
        zzz[i] = c[i - 192];
    }
    for (i = 224; i < 256; i++) {
        zzz[i] = d[i - 224];
    }
}
char *jstringToChar(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = (*env).FindClass("java/lang/String");
    jstring strencode = (*env).NewStringUTF("utf-8");
    jmethodID mid = (*env).GetMethodID(clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) (*env).CallObjectMethod(jstr, mid, strencode);
    jsize alen = (*env).GetArrayLength(barr);
    jbyte *ba = (*env).GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    (*env).ReleaseByteArrayElements(barr, ba, 0);
    return rtn;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_connect(JNIEnv *env, jobject thiz, jstring server, jint port,
                                             jstring device_id) {
    callJava = new CallJava(env, jvm, &thiz);
    udxTcpSinkP2P.initUdx();
    udxTcpSinkP2P.Register(jstringToChar(env, server), port, jstringToChar(env, device_id));
    getchar();
    callJava->conn(THREAD_MAIN);
}

void byteLink(BYTE *string, BYTE *string1, BYTE *string2) {
    int i;
    int s1 = sizeof(string);
    int s2 = sizeof(string1);
    for(i=0;i<s1;i++){
        string2[i] = string[i];
    }
    for(i=s1;i<s1+s2;i++) {
        string2[i] = string1[i+s1];
    }
}

extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_send(JNIEnv *env, jobject thiz, jbyteArray data, jlong len,
                                          jboolean key_frame) {

    uint8_t isCopy = 1;

    jbyte *olddata = (jbyte *) env->GetByteArrayElements(data, &isCopy);
    jsize oldsize = env->GetArrayLength(data);
    BYTE *bytearr = (BYTE *) olddata;
    udxTcpSinkP2P.SendFrame(1, key_frame, bytearr, oldsize);
}extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_stop(JNIEnv *env, jobject thiz) {
    udxTcpSinkP2P.shutdownloop();
}extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_sendAudio(JNIEnv *env, jobject thiz, jbyteArray data,
                                               jlong len) {
//    jbyte *olddata = env->GetByteArrayElements(data, 0);
//    udxTcpSinkP2P.SendFrame(0, 0, (BYTE *) olddata, len);
}extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_sendSPSPPS(JNIEnv *env, jobject thiz, jbyteArray sps,
                                                jbyteArray pps) {
    uint8_t isCopy = 1;
    jbyte *oldsps = (jbyte *) env->GetByteArrayElements(sps, &isCopy);
    jsize spssize = env->GetArrayLength(sps);
    BYTE *bytesps = (BYTE *) oldsps;
    jbyte *oldpps = (jbyte *) env->GetByteArrayElements(pps, &isCopy);
    jsize ppssize = env->GetArrayLength(pps);
    BYTE *bytepps = (BYTE *) oldpps;
    int totalSize = spssize+ppssize;
    BYTE * total =new BYTE [totalSize];
    for(int i=0;i<totalSize;i++){
        if(i<spssize){
            total[i] = bytesps[i];
        } else {
            total[i] = bytepps[i-spssize];
        }
    }
    udxTcpSinkP2P.SendFrame(1, 0, total, ppssize+spssize);
}