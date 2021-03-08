package io.e4x.exliver

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var mVirtualDisplay: VirtualDisplay
    private lateinit var mMediaProjectionCallback: MediaProjectionCallback
    private var mProjectionManager: MediaProjectionManager? = null
    private var mScreenDensity: Int = 0
    private var mMediaRecorder: MediaRecorder? = null
    private var mMediaProjection: MediaProjection? = null
    lateinit var mMediaProjectionManager:MediaProjectionManager
    lateinit var recordBounds:Rect
    private lateinit var btnRecorder: ToggleButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        recordBounds = Rect()
        windowManager.getDefaultDisplay().getRectSize(recordBounds);
        btnRecorder = findViewById<ToggleButton>(R.id.fab)
        // Build ScreenRecorder
        val rxPermissions = RxPermissions(this)
        val disposble = rxPermissions.request(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ).subscribe {
            if (it) {
                createRecorder()
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_SCREEN_RECORDER -> {
                data?.let {
                    mMediaProjection = mProjectionManager!!.getMediaProjection(resultCode, data!!)
                    mMediaProjection!!.registerCallback(mMediaProjectionCallback, null)
                    mVirtualDisplay = createVirtualDisplay()
                    mMediaRecorder!!.start()
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
    fun createRecorder() {
        val metrics = DisplayMetrics()
        getWindowManager().getDefaultDisplay().getMetrics(metrics)
        mScreenDensity = metrics.densityDpi

        initRecorder()
        prepareRecorder()
        mProjectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?

        btnRecorder.setOnClickListener { v -> onToggleScreenShare(v) }
        mMediaProjectionCallback = MediaProjectionCallback()
    }

    private fun onToggleScreenShare(v: View?) {
        if ((v as ToggleButton).isChecked) {
            shareScreen()
        } else {
            mMediaRecorder!!.stop()
            mMediaRecorder!!.reset()
            Log.v(TAG, "Recording Stopped")
            stopScreenSharing()
        }
    }

    fun initRecorder(){
        if (mMediaRecorder == null) {
            mMediaRecorder = MediaRecorder()
            mMediaRecorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder!!.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder!!.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mMediaRecorder!!.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB)
            mMediaRecorder!!.setVideoEncodingBitRate(512 * 4000)
            mMediaRecorder!!.setVideoFrameRate(30)
            mMediaRecorder!!.setVideoSize(recordBounds.width(), recordBounds.height())
            mMediaRecorder!!.setOutputFile(getFilePath())
        }
    }
    private fun prepareRecorder() {
        try {
            mMediaRecorder!!.prepare()
        } catch (e: IllegalStateException) {
            e.printStackTrace()
            finish()
        } catch (e: IOException) {
            e.printStackTrace()
            finish()
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
        var time = System.currentTimeMillis().toShort()
        return outputPath + File.separator + "mv" + time + ".mp4"
    }
    private fun shareScreen() {
        if (mMediaProjection == null) {
            startActivityForResult(
                    mProjectionManager!!.createScreenCaptureIntent(),
                    REQUEST_SCREEN_RECORDER
            )
            return
        }
        if(mMediaRecorder == null)
        {
            initRecorder()
            prepareRecorder()
        }
        mVirtualDisplay = createVirtualDisplay()
        mMediaRecorder!!.start()
    }

    private fun stopScreenSharing() {
        if (mVirtualDisplay == null) {
            return
        }
        mVirtualDisplay!!.release()
        mMediaRecorder=null
        //    mMediaRecorder!!.release()
    }

    private fun createVirtualDisplay(): VirtualDisplay {
        return mMediaProjection!!.createVirtualDisplay(
                "MainActivity",
                recordBounds.width(), recordBounds.height(), mScreenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder?.surface, null /*Callbacks*/, null /*Handler*/
        )
    }
    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            if (btnRecorder!!.isChecked) {
                btnRecorder!!.isChecked = false
                mMediaRecorder!!.stop()
                mMediaRecorder!!.reset()
                Log.v(TAG, "Recording Stopped")
            }
            mMediaProjection = null
            stopScreenSharing()
            Log.i(TAG, "MediaProjection Stopped")
        }
    }

    companion object{
        private const val REQUEST_SCREEN_RECORDER = 100
        private val STORAGE_FOLDER_NAME = "records"
        private val TAG = MainActivity::class.java.simpleName
    }
}