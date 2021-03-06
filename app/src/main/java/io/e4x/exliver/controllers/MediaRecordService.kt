package io.e4x.exliver.controllers
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.util.Log

/**
 * 媒体录制服务
 */
class MediaRecordService (
        private val width: Int,
        private val height: Int,
        private val bitrate: Int,
        private val dpi: Int,
        private val mediaProjection: MediaProjection?,
        private val destPath: String
): Thread() {

    companion object {
        const val FRAME_RATE = 60
        const val TAG = "MediaRecordService"
    }

    private var mMediaRecorder: MediaRecorder ?= null
    private var mVirtualDisplay: VirtualDisplay ?= null

    override fun run() {
        super.run()
        initMediaRecorder()
        mVirtualDisplay = mediaProjection?.createVirtualDisplay(
                TAG,
                width,
                height,
                dpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mMediaRecorder?.surface,
                null,
                null
        )
        mMediaRecorder?.start()
    }

    /**
     * 初始化MediaRecorder
     *
     * @return
     */
    private fun initMediaRecorder() {
        mMediaRecorder = MediaRecorder()
        mMediaRecorder?.apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
//            setAudioSource(MediaRecorder.AudioSource.MIC) // 需要 Audio 权限，我们暂时不需要录音，只录屏
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(destPath)
            setVideoSize(width, height)
            setVideoFrameRate(FRAME_RATE)
            setVideoEncodingBitRate(bitrate)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
//            setAudioEncoder(MediaRecorder.AudioEncoder.AAC) // 需要 Audio 权限，我们暂时不需要录音，只录屏
            try {
                mMediaRecorder?.prepare()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Log.i(TAG, "media recorder $bitrate kps")
        }
    }

    fun release() {
        if (mVirtualDisplay != null) {
            mVirtualDisplay?.release()
            mVirtualDisplay = null
        }
        if (mMediaRecorder != null) {
            mMediaRecorder?.setOnErrorListener(null)
            mediaProjection?.stop()
            mMediaRecorder?.reset()
            mMediaRecorder?.release()
        }
        mediaProjection?.stop()
        Log.i(TAG, "release")
    }
}

