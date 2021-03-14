package io.e4x.exliver

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Rect
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import io.e4x.exliver.vo.RecordVO
import live.rtmp.OnConntionListener
import live.rtmp.RtmpHelper
import live.rtmp.encoder.BasePushEncoder
import live.rtmp.encoder.PushEncode
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity(), OnConntionListener, BasePushEncoder.OnMediaInfoListener {

    private lateinit var pushEncode: PushEncode
    private var rtmpHelper: RtmpHelper? = null
    private var livingURL = "rtmp://81.69.31.51:1935/oflaDemo/"
    private lateinit var msgBinder: RecordService.MsgBinder
    private lateinit var recordService: RecordService
    private lateinit var mMediaProjectionCallback: MediaProjectionCallback
    private var mProjectionManager: MediaProjectionManager? = null
    private var mScreenDensity: Int = 0
    private var mMediaProjection: MediaProjection? = null
    lateinit var recordBounds:Rect
    private lateinit var btnRecorder: ToggleButton
    private var listRecord: MutableList<RecordVO> = ArrayList()
    private var liveUrl:String = ""

    private var serviceConnection = object :ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            recordService = (service as RecordService.MsgBinder)?.getService()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            TODO("Not yet implemented")
        }

    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        recordBounds = Rect()
        windowManager.getDefaultDisplay().getRectSize(recordBounds);
        btnRecorder = findViewById<ToggleButton>(R.id.fab)
        // Build ScreenRecorder
        val rxPermissions = RxPermissions(this)
        rxPermissions.request(
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).subscribe {
            if (it) {
                var intent = Intent(this, RecordService::class.java).apply {
                    action = RecordService.ACTION_INIT
                    putExtra(RecordService.SCREEN_SIZE_WIDTH, 1280)
                    putExtra(RecordService.SCREEN_SIZE_HEIGHT, 720)
                }
                startService(intent)
                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
                btnRecorder.setOnClickListener { v -> onToggleScreenRecord(v) }
//                createLiver()
            } else {
                Toast.makeText(this, "请设置权限", Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_SCREEN_RECORDER -> {
                data?.let {
                    try {
                        mMediaProjection =
                            mProjectionManager!!.getMediaProjection(resultCode, data!!)
                        mMediaProjection!!.registerCallback(mMediaProjectionCallback, null)
                        rtmpHelper = RtmpHelper()
                        rtmpHelper?.setOnConntionListener(this)

                        var time = System.currentTimeMillis().toString()
                        var recordName = "mv" + System.currentTimeMillis().toString()
                        var newRecord = RecordVO(time, recordName)
                        var liveUrl = livingURL + recordName
                        listRecord.add(newRecord)
                        rtmpHelper?.initLivePush(liveUrl)

                    } catch(e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
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
    override fun onDestroy() {
        super.onDestroy()
        if (mMediaProjection != null) {
            mMediaProjection!!.stop()
            mMediaProjection = null
        }
    }
    fun getFilePath():String{
        var outputPath = getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath + File.separator + STORAGE_FOLDER_NAME
        var folder = File(outputPath)
        if(!folder.exists()) {
            if(!folder.mkdir()){
                throw IOException("folder can't be create")
            }
        }
        var time = System.currentTimeMillis().toString()
        return outputPath + File.separator + "mv" + time + ".mp4"
    }
    fun createLiver() {
        if (mScreenDensity == 0) {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            mScreenDensity = metrics.densityDpi
        }

        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?

        btnRecorder.setOnClickListener { v -> onToggleScreenShare(v) }
        mMediaProjectionCallback = MediaProjectionCallback()

        btnRecorder.isChecked = true
    }
    private fun onToggleScreenShare(v: View?) {
        if ((v as ToggleButton).isChecked) {
            startLiving()
        } else {
            // stop living
            stopLiving()
        }
    }
    private fun onToggleScreenRecord(v: View?) {
        if ((v as ToggleButton).isChecked) {
            recordService.start(getFilePath())
        } else {
            recordService.stop()
        }
    }
    private fun startLiving() {
        if (mMediaProjection == null) {
            startActivityForResult(
                    mProjectionManager!!.createScreenCaptureIntent(),
                    REQUEST_SCREEN_RECORDER
            )
        }
        Log.v(TAG, "living start : " + liveUrl)
    }
    private fun stopLiving() {
        // stop living
        rtmpHelper?.stop()
        mMediaProjection?.stop()
        rtmpHelper = null
        mMediaProjection = null
        Log.v(TAG, "living Stopped")
    }
    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            if (btnRecorder!!.isChecked) {
                btnRecorder!!.isChecked = false
                // mMediaRecorder.stop
                Log.v(TAG, "Recording Stopped")
            }
//            mMediaProjection = null
            Log.i(TAG, "MediaProjection Stopped")
        }
    }

    override fun onConntecting() {
        Log.e(TAG, "RTMPHelper onConntecting")
    }

    override fun onConntectSuccess() {
        Log.e(TAG, "RTMPHelper onConntectSuccess")
        startPush()
    }
    private fun startPush() {
        pushEncode = PushEncode(this)
        pushEncode.initEncoder(true, mMediaProjection, 480, 720, 44100, 2, 16)
        pushEncode.setOnMediaInfoListener(this)
        pushEncode.start()
    }

    override fun onConntectFail(msg: String?) {
        Log.e(TAG, "PushEncode onConntectFail")
        btnRecorder.isChecked = false
    }

    override fun onMediaTime(times: Int) { }

    override fun onSPSPPSInfo(sps: ByteArray?, pps: ByteArray?) {
        if (rtmpHelper == null) return
        rtmpHelper?.pushSPSPPS(sps, pps)
    }

    override fun onVideoDataInfo(data: ByteArray?, keyFrame: Boolean) {
        if (rtmpHelper == null) return
        rtmpHelper?.pushVideoData(data, keyFrame)
    }

    override fun onAudioInfo(data: ByteArray?) {
        if (rtmpHelper == null) return
        rtmpHelper?.pushAudioData(data)
    }

    companion object{
        private const val RECORD_LIST_SIZE = 30
        private const val REQUEST_SCREEN_RECORDER = 1
        private val STORAGE_FOLDER_NAME = "records"
        private val TAG = MainActivity::class.java.simpleName
    }

}