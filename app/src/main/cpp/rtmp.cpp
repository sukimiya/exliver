//
// Created by sukim on 2021/4/7.
//

#include <jni.h>
#include "UdxTcpSink_P2p.h"

UdxTcpSink_P2p udxTcpSinkP2P;

char *jstringToChar(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = (*env).FindClass( "java/lang/String");
    jstring strencode = (*env).NewStringUTF( "utf-8");
    jmethodID mid = (*env).GetMethodID( clsstring, "getBytes", "(Ljava/lang/String;)[B");
    jbyteArray barr = (jbyteArray) (*env).CallObjectMethod( jstr, mid, strencode);
    jsize alen = (*env).GetArrayLength( barr);
    jbyte *ba = (*env).GetByteArrayElements( barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1);
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    (*env).ReleaseByteArrayElements( barr, ba, 0);
    return rtn;
}

extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_connect(JNIEnv *env, jobject thiz, jstring server, jint port,
        jstring device_id) {
    udxTcpSinkP2P.initUdx();
    udxTcpSinkP2P.Register(jstringToChar(env, server), port, jstringToChar(env, device_id));
    getchar();
}extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_send(JNIEnv *env, jobject thiz, jbyteArray data, jlong len,
                                          jboolean key_frame) {

    jbyte * olddata = env->GetByteArrayElements(data, 0);
    udxTcpSinkP2P.SendFrame(1, key_frame, (BYTE*)olddata, len);
}extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_stop(JNIEnv *env, jobject thiz) {
    udxTcpSinkP2P.shutdownloop();
}extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_sendAudio(JNIEnv *env, jobject thiz, jbyteArray data,
                                               jlong len) {
    jbyte * olddata = env->GetByteArrayElements(data, 0);
    udxTcpSinkP2P.SendFrame(0, 0, (BYTE*)olddata, len);
}