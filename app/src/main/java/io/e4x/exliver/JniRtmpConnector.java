package io.e4x.exliver;

import org.jetbrains.annotations.Nullable;

import live.rtmp.OnConntionListener;

public class JniRtmpConnector {
    public void initConnect(String server, int port, String deviceId) {
        connect(server, port, deviceId);
    }
    public void sendData(byte[] data, boolean keyFrame) {
        send(data, data.length, keyFrame);
    }
    public void sendAudioData(byte[] data) {
        sendAudio(data, data.length);
    }
    public void disconnect() {
        stop();
    }
    private OnConntionListener mOnConntionListener;
    static {
        System.loadLibrary("MFastUdx");
    }
    public void onConntecting() {
        if (mOnConntionListener != null) {
            mOnConntionListener.onConntecting();
        }
    }

    public void onConntectSuccess() {
        if (mOnConntionListener != null) {
            mOnConntionListener.onConntectSuccess();
        }
    }

    public void onConntectFail(String msg) {
        if (mOnConntionListener != null) {
            mOnConntionListener.onConntectFail(msg);
        }
    }

    public void setOnConntionListener(OnConntionListener onConntionListener) {
        this.mOnConntionListener = onConntionListener;
    }

    private native void connect(String server, int port, String deviceId);
    private native void send(byte[] data, long len, boolean keyFrame);
    private native void sendAudio(byte[] data, long len);
    private native void stop();

    public void pushSPSPPS(@Nullable byte[] sps, @Nullable byte[] pps) {

    }

    public void pushVideoData(@Nullable byte[] data, boolean keyFrame) {
        sendData(data, keyFrame);
    }

    public void pushAudioData(@Nullable byte[] data) {
        sendAudioData(data);
    }
}
