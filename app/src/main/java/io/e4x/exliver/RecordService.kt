package io.e4x.exliver

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.android.camera.utils.SmartSize
import io.e4x.exliver.controllers.RecordFileReader
import io.e4x.exliver.controllers.RecordUploader
import io.e4x.exliver.net.UploadServices
import io.e4x.exliver.net.rx
import io.e4x.exliver.utils.Bitrate
import io.e4x.exliver.utils.DeviceHelper
import io.e4x.exliver.utils.FileUtil
import io.e4x.exliver.vo.RecordVO
import live.rtmp.OnConntionListener
import live.rtmp.RtmpHelper
import live.rtmp.encoder.BasePushEncoder
import live.rtmp.encoder.PushEncode
import java.io.File
import java.io.IOException
import java.util.*


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
class RecordService : Service() {

    private val NOTIFICATION_ID = 1
    private lateinit var fileUtil: FileUtil
    private lateinit var recordFileReader: RecordFileReader
    private lateinit var recordUploader: RecordUploader
    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var mediaProjection: MediaProjection
    private var mediaRecorder: MediaRecorder? = null
    private var timer: Timer = Timer()
    private var timerOn: Boolean = false
    private var displayMetrics: DisplayMetrics? = null
    private var screenSize: Size? = null
    private var isInitialized = false
    private var virtualDisplay: VirtualDisplay? = null
    private lateinit var upstreamUrl:String
    private lateinit var pushEncode: PushEncode
    var player: MediaPlayer?=null
    @SuppressLint("CheckResult")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate() {
        super.onCreate()
        fileUtil = FileUtil(this)
        recordFileReader = RecordFileReader(this)
        recordUploader = RecordUploader()
        // 获取服务通知
        player =MediaPlayer.create(this,R.raw.bgm);
        player?.setOnCompletionListener {
            fun onCompletion(mp: MediaPlayer?){
                Toast.makeText(this,"完成音频播放...",Toast.LENGTH_SHORT).show()
            }
        }
        player?.isLooping = true
        // 获取服务通知
        var notification: Notification = createForegroundNotification()
        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        //将服务置于启动状态 ,NOTIFICATION_ID指的是创建的通知的ID
        startForeground(NOTIFICATION_ID, notification)
        Toast.makeText(this,"开始播放...",Toast.LENGTH_SHORT).show();//提示框
    }
    private lateinit var rtmpHelper:JniRtmpConnector
    private var tempFrame:Boolean = false

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_INIT -> {
                var w = intent?.getIntExtra(SCREEN_SIZE_WIDTH, 1280)
                var h = intent?.getIntExtra(SCREEN_SIZE_HEIGHT, 720)
                screenSize = SmartSize(w, h).size
//                val mediaPermission = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
//                initService(intent)
            }
//            ACTION_START -> {
//                var path = intent?.getStringExtra(RECORD_PATH)
//                if (path != null) {
//                    perparRecording(path)
//                    start()
//                }
//            }
            ACTION_STOP -> {
                if (isInitialized) {
                    stop()
                }
            }
        }
        when (intent?.getIntExtra("play", -1)){
            1 -> {
                player?.start()//开始
            }
            2 -> {
                if (player!=null&&player?.isPlaying()!!){
                    player?.pause()
                } else {
                    player?.start()
                }
            }
            3 -> {
                player?.stop();//停止
                player?.release();//释放内存
            }
        }
//        return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBind(intent: Intent?): IBinder? {
        return MsgBinder(this, RecordService::class.java.simpleName + System.currentTimeMillis().toString())
    }

    override fun onDestroy() {
        super.onDestroy()
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    class MsgBinder(service: RecordService, descriptor: String?) : Binder(descriptor) {
        private var mService = service
        fun getService(): RecordService {
            return mService
        }
        fun getDuration(): Int? {//获取总进度条
            return mService.player?.getDuration();
        }
        fun getCurrentPosition(): Int? {//获取当前进度条
            return mService.player?.getCurrentPosition();
        }
        fun setProgress(s:Int){//更改当前音乐进度
            mService.player?.seekTo(s);
        }
    }
    @SuppressLint("CheckResult")
    fun initService(mediaPermission: Intent, metrics: DisplayMetrics) {
        displayMetrics = metrics
        projectionManager = (getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager?)!!
        mediaProjection = projectionManager.getMediaProjection(Activity.RESULT_OK, mediaPermission!!)
        mediaProjection.registerCallback(object : MediaProjection.Callback() {
            @SuppressLint("LongLogTag")
            override fun onStop() {
                Log.d("MediaProjection:Callback", "onStop")
                super.onStop()
            }
        }, null)
        initRecorder(metrics)
        UploadServices.getInstance(this).getPushUrl().rx().subscribe(){
            upstreamUrl = it.resault
            createLive()
        }
    }
    private fun createLive() {
        var self = this
        rtmpHelper = JniRtmpConnector()
        rtmpHelper.initConnect("e4x.live", 1935, UploadServices.getInstance(this).getDeviceId())
        rtmpHelper.setOnConntionListener(object : OnConntionListener{
            override fun onConntectFail(msg: String?) {
                TODO("Not yet implemented")
            }

            override fun onConntecting() {
                pushEncode = PushEncode(self)
                pushEncode.initEncoder(true, mediaProjection, screenSize?.width!!, screenSize?.height!!, 44100, 2, 16)
                pushEncode.setOnMediaInfoListener(object: BasePushEncoder.OnMediaInfoListener {
                    override fun onMediaTime(times: Int) { }

                    override fun onSPSPPSInfo(sps: ByteArray?, pps: ByteArray?) {
                        rtmpHelper.pushSPSPPS(sps, pps)
                    }

                    override fun onVideoDataInfo(data: ByteArray?, keyFrame: Boolean) {
                        rtmpHelper.pushVideoData(data,keyFrame)
                    }

                    override fun onAudioInfo(data: ByteArray?) {
                        rtmpHelper.pushAudioData(data)
                    }
                })
            }

            override fun onConntectSuccess() {
                TODO("Not yet implemented")
            }

        })
    }
    fun perparRecording(path: String) {
        currentFilePath = path
        if (mediaRecorder != null) {
            mediaRecorder?.setOutputFile(currentFilePath)
            try {
                mediaRecorder?.prepare()
            } catch (e: IOException) {
                Log.e(TAG, "prepare() failed")
            }
        }
    }
    private fun initRecorder(metrics: DisplayMetrics) {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        // H265
        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.HEVC)
        mediaRecorder?.setVideoEncodingBitRate(Bitrate.videoBitRate(screenSize!!.width, screenSize!!.height))
        mediaRecorder?.setVideoFrameRate(25)
        mediaRecorder?.setVideoSize(screenSize!!.width, screenSize!!.height)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder?.setAudioSamplingRate(44100)
    }
    /**
     * 创建服务通知
     */
    private fun createForegroundNotification():Notification {
        // 唯一的通知通道的id.
        var notificationChannelId = "notification_channel_id_01";

        // Android8.0以上的系统，新建消息通道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //用户可见的通道名称
            var channelName = "Foreground Service Notification";
            //通道的重要程度
            var importance = NotificationManager.IMPORTANCE_HIGH;
            var notificationChannel =
                NotificationChannel(notificationChannelId, channelName, importance);
            notificationChannel.description = "Channel description";
            //LED灯
            notificationChannel.enableLights(true);
            notificationChannel.lightColor = Color.RED;
            //震动
//            notificationChannel.vibrationPattern = longArrayOf(0L, 1000L, 500L, 1000L);
//            notificationChannel.enableVibration(true);
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)?.createNotificationChannel(
                notificationChannel
            )
        }
        val builder =
            NotificationCompat.Builder(this, notificationChannelId)
        //通知小图标
        //通知小图标
        builder.setSmallIcon(R.drawable.ic_launcher_foreground)
//        var notiView = RemoteViews(packageName, R.layout.notification)
//        notiView.setTextViewText(R.id.noti_title, "Exliver")
//        notiView.setTextViewText(R.id.noti_content, "Capture Screen and push to live!")
//        val iintent = Intent(this, MainActivity::class.java)
//        iintent.action = ACTION_STOP
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            notiView.setOnClickResponse(R.id.noti_toggle, RemoteViews.RemoteResponse.fromPendingIntent(PendingIntent.getActivity(this, 0,
//                    iintent, PendingIntent.FLAG_UPDATE_CURRENT)))
//        } else {
//            notiView.setOnClickPendingIntent(R.id.noti_toggle, PendingIntent.getActivity(this, 0,
//                    iintent, PendingIntent.FLAG_UPDATE_CURRENT))
//        }
//        builder.setContent(notiView)
        //通知标题
        //通知标题
        builder.setContentTitle("exliver")
        //通知内容
        //通知内容
        builder.setContentText("recorder")
        //设定通知显示的时间
        //设定通知显示的时间
        builder.setWhen(System.currentTimeMillis())
        //设定启动的内容
        //设定启动的内容
        val activityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(this, 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        builder.setContentIntent(pendingIntent)

        //创建通知并返回

        //创建通知并返回
        return builder.build()
    }
    fun createVirtualDisplay(metrics: DisplayMetrics): VirtualDisplay{
        virtualDisplay =  mediaProjection?.createVirtualDisplay(
            TAG, metrics.widthPixels,
            metrics.heightPixels, metrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mediaRecorder?.surface, null, null
        )
        return virtualDisplay!!
    }
    private fun uploadRecord() {
        var filepath = currentFilePath.split(File.separator)
        var filename = filepath.get(filepath.size-1)
        var time = filename.substring(2, filename.lastIndex - 3)
        var recordvo = RecordVO(time, currentFilePath)
        recordFileReader.add(recordvo)
        recordFileReader.removeOld()
        // upload to server
        recordUploader.upload(recordvo)
    }
    private var currentFilePath = ""
    fun start() {
        if (timerOn) {
            stop()
            timerOn = false
        }
        if (mediaRecorder!=null) {
            if(virtualDisplay == null)
                virtualDisplay = createVirtualDisplay(displayMetrics!!)
            else
                virtualDisplay!!.surface = mediaRecorder?.surface
            mediaRecorder?.start()
            timer = Timer()
            timer.schedule(object : TimerTask() {
                override fun run() {
                    timer.cancel()
                    timerOn = false
                    stop()
                    resume()
                }
            }, RECORDING_DURATION)
            isStoped = false
            timerOn = true
            Log.d(TAG, "start record with file: $currentFilePath")
        }
    }
    private var isStoped = true
    fun stop() {
        if (timerOn) {
            timer.cancel()
            timerOn = false
        }
        if (!isStoped) {
            mediaRecorder?.setOnErrorListener(null)
            mediaRecorder?.setOnInfoListener(null)
            mediaRecorder?.setPreviewDisplay(null)
            mediaRecorder?.stop()
            mediaRecorder?.reset()
            mediaRecorder?.release()
            isStoped = true
            uploadRecord()
            Log.d(TAG, "stop record with file: $currentFilePath")
        }
    }
    fun resume() {
        initRecorder(displayMetrics!!)
        currentFilePath = fileUtil.getMVFilePath()
        perparRecording(currentFilePath)
        start()
    }

    companion object {
        const val TAG = "RecordService"

        const val ACTION_INIT = "io.e4x.exliver.action.RECORD_INIT"
        const val ACTION_START = "io.e4x.exliver.action.RECORD_START"
        const val ACTION_STOP = "io.e4x.exliver.action.RECORD_STOP"
        const val SCREEN_SIZE_WIDTH = "io.e4x.exliver.extra.SCREEN_SIZE_WIDTH"
        const val SCREEN_SIZE_HEIGHT = "io.e4x.exliver.extra.SCREEN_SIZE_HEIGHT"
        const val RECORD_PATH = "io.e4x.exliver.extra.RECORD_PATH"

        private const val REQUEST_SCREEN_RECORDER = 1
        // record 360s
        private const val RECORDING_DURATION = 120L * 1000L

    }
    private inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {

            Log.i(TAG, "MediaProjection Stopped")
        }
    }
}