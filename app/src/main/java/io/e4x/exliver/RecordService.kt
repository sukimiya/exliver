package io.e4x.exliver

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.media.MediaRecorder
import android.os.Binder
import android.os.IBinder
import android.util.Log
import android.util.Size
import com.example.android.camera.utils.SmartSize
import io.e4x.exliver.utils.Bitrate

// TODO: Rename actions, choose action names that describe tasks that this
// IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
private const val ACTION_FOO = "io.e4x.exliver.action.FOO"
private const val ACTION_BAZ = "io.e4x.exliver.action.BAZ"

// TODO: Rename parameters
private const val EXTRA_PARAM1 = "io.e4x.exliver.extra.PARAM1"
private const val EXTRA_PARAM2 = "io.e4x.exliver.extra.PARAM2"

/**
 * An [IntentService] subclass for handling asynchronous task requests in
 * a service on a separate handler thread.

 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.

 */
class RecordService : IntentService("RecordService") {

    private lateinit var mediaRecorder: MediaRecorder
    private var screenSize: Size? = null
    private var isInitialized = false
    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_INIT -> {
                var w = intent?.getIntExtra(SCREEN_SIZE_WIDTH, 1280)
                var h = intent?.getIntExtra(SCREEN_SIZE_HEIGHT, 720)
                screenSize = SmartSize(w, h).size
                initRecorder()
            }
            ACTION_START -> {
                if(!isInitialized) initRecorder()
                var path = intent?.getStringExtra(RECORD_PATH)
                start(path)
            }
            ACTION_STOP -> {
                if (!isInitialized) return
                stop()
            }
            ACTION_FOO -> {
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)

                handleActionFoo(param1, param2)
            }
            ACTION_BAZ -> {
                val param1 = intent.getStringExtra(EXTRA_PARAM1)
                val param2 = intent.getStringExtra(EXTRA_PARAM2)
                handleActionBaz(param1, param2)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MsgBinder(this, RecordService::class.java.simpleName + System.currentTimeMillis().toString())
    }
    class MsgBinder(service: RecordService, descriptor: String?) : Binder(descriptor) {
        private var mService = service
        fun getService(): RecordService {
            return mService
        }
    }
    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionFoo(param1: String, param2: String) {
        TODO("Handle action Foo")
    }
    private fun initRecorder() {
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        // H265
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.HEVC)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.setVideoEncodingBitRate(Bitrate.videoBitRate(screenSize!!.width, screenSize!!.height))
        mediaRecorder.setVideoFrameRate(25)
        mediaRecorder.setVideoSize(screenSize!!.width, screenSize!!.height)

    }
    private var currentFilePath = ""
    fun start(path:String) {
        currentFilePath = path
        mediaRecorder.setOutputFile(currentFilePath)
        mediaRecorder.start()
        Log.d(TAG, "start record with file: $currentFilePath")
    }
    fun stop() {
        mediaRecorder.stop()
        Log.d(TAG, "stop record with file: $currentFilePath")
    }
    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionBaz(param1: String, param2: String) {
        TODO("Handle action Baz")
    }

    companion object {
        const val TAG = "RecordService"

        const val ACTION_INIT = "io.e4x.exliver.action.RECORD_INIT"
        const val ACTION_START = "io.e4x.exliver.action.RECORD_START"
        const val ACTION_STOP = "io.e4x.exliver.action.RECORD_STOP"
        const val SCREEN_SIZE_WIDTH = "io.e4x.exliver.extra.SCREEN_SIZE_WIDTH"
        const val SCREEN_SIZE_HEIGHT = "io.e4x.exliver.extra.SCREEN_SIZE_HEIGHT"
        const val RECORD_PATH = "io.e4x.exliver.extra.RECORD_PATH"
        /**
         * Starts this service to perform action Foo with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionFoo(context: Context, param1: String, param2: String) {
            val intent = Intent(context, RecordService::class.java).apply {
                action = ACTION_FOO
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }

        /**
         * Starts this service to perform action Baz with the given parameters. If
         * the service is already performing a task this action will be queued.
         *
         * @see IntentService
         */
        // TODO: Customize helper method
        @JvmStatic
        fun startActionBaz(context: Context, param1: String, param2: String) {
            val intent = Intent(context, RecordService::class.java).apply {
                action = ACTION_BAZ
                putExtra(EXTRA_PARAM1, param1)
                putExtra(EXTRA_PARAM2, param2)
            }
            context.startService(intent)
        }
    }
}