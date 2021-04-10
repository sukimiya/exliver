package live.rtmp;

public interface OnConntionListener {

    void onConntecting(String msg);

    void onConntectSuccess();

    void onConntectFail(String msg);
}
