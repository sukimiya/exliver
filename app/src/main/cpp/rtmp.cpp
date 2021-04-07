//
// Created by sukim on 2021/4/7.
//

#include <jni.h>

extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_connect(JNIEnv *env, jobject thiz, jstring server, jint port,
        jstring device_id) {
// TODO: implement connect()
}extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_send(JNIEnv *env, jobject thiz, jbyteArray data, jlong len,
                                          jboolean key_frame) {
    // TODO: implement send()
}extern "C"
JNIEXPORT void JNICALL
Java_io_e4x_exliver_JniRtmpConnector_stop(JNIEnv *env, jobject thiz) {
    // TODO: implement stop()
}