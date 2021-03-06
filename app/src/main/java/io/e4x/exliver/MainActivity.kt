package io.e4x.exliver

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Rect
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.screenrecorder.helper.ScreenRecorderBuild
import com.android.screenrecorder.helper.ScreenStateListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.tbruyelle.rxpermissions2.RxPermissions
import io.e4x.exliver.model.MessageBus
import io.e4x.exliver.utils.Utils
import io.e4x.exliver.views.ScreenService
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class MainActivity : AppCompatActivity(), ScreenStateListener {

    lateinit var mMediaProjectionManager:MediaProjectionManager
    lateinit var recordBounds:Rect
    private var bitrate:Int = 1 * 1024 * 800
    private var screenRecorderBuild: ScreenRecorderBuild? = null
    private var myService:ScreenService? = null
    /**
     * 把 service 链接起来
     * 拿到 service 对象
     */
    private val connection: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {}
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            //返回一个MsgService对象
            myService = (service as ScreenService.MyBinder).getService()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        recordBounds = Rect()
        windowManager.getDefaultDisplay().getRectSize(recordBounds);
        bitrate = 1 * recordBounds.width() * recordBounds.height()
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { view ->
            requestScreenRecorder()
            Snackbar.make(view, "Start Record", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }

        // regist EventBus
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        // Build ScreenRecorder
        val rxPermissions = RxPermissions(this)
        val disposble = rxPermissions.request(
            Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.SYSTEM_ALERT_WINDOW,
            Manifest.permission.ACCESS_NOTIFICATION_POLICY
        ).subscribe {
            screenRecorderBuild = ScreenRecorderBuild.Builder()
                .setActivity(this)
                .setFps(Utils.getFps())
                .setBitRate(Utils.getBitRate())
                .setStateListener(this)
                .build()
        }
    }
    private fun requestScreenRecorder() {
        mMediaProjectionManager =
                getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(
            mMediaProjectionManager.createScreenCaptureIntent(),
            REQUEST_SCREEN_RECORDER
        )
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_SCREEN_RECORDER -> {
                data?.let {
                    screenRecorderBuild?.onActivityResult(requestCode, resultCode, data)
                }
                if (requestCode == 0) {
                    if (!Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show()
                        startService(Intent(this@MainActivity, ScreenService::class.java))
                    }
                }
            }
        }
    }

    //    override fun onPause() {
//        if(mMediaRecordService!=null && mMediaRecordService.isAlive)
//            mMediaRecordService.stop()
//        super.onPause()
//    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetMessages(messageBus: MessageBus) {
        if (messageBus.msg == MessageBus.START) {
            screenRecorderBuild?.startRecord()
            if (!Utils.isForeground(this)) {
                startActivity(Intent(this, ActivityHidden::class.java))
            }

        }
        if (messageBus.msg == MessageBus.STOP) {
            screenRecorderBuild?.stopRecord()
        }
        if (messageBus.msg == MessageBus.PAUSE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                screenRecorderBuild?.pause()
            }
        }
        if (messageBus.msg == MessageBus.CONTINUES) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                screenRecorderBuild?.resume()
            }
        }
        if (messageBus.msg == MessageBus.INIT) {
            screenRecorderBuild?.setBitRate(Utils.getBitRate())
            screenRecorderBuild?.setFps(Utils.getFps())
            screenRecorderBuild?.setAudioVoice(Utils.getMicrophone())
            Log.e("MainActivity", "init")
        }
    }

    override fun recording() {
        Log.d(TAG, "recording start")
    }

    override fun pause() {
        Log.d(TAG, "recording pause")
    }

    override fun stop(path: String?) {
        Toast.makeText(this, "文件以保存到${path}", Toast.LENGTH_SHORT).show()
    }

    override fun error(msg: String?) {
        Log.e(TAG, msg!!)
    }
    companion object{
        private const val REQUEST_SCREEN_RECORDER = 100
        private val TAG = MainActivity::class.java.simpleName
    }
}