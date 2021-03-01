package io.e4x.exliver.controllers

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.Surface
import io.e4x.exliver.vo.CameraSurface

class CamManager(cameraManager: CameraManager) {
    private val cameraThreadNameTitle = "CameraThread"
    private var cameraManager :CameraManager = cameraManager
    fun checkAvalibleCameras(list: List<CameraSurface>):MutableList<CameraSurface>{
        var enumerateList = enumerateCameras(cameraManager)
        return list.filter {
            enumerateList.contains(it.cameraId)
        }.toMutableList()
    }
    private fun getCameraHandler(id: String): Handler {
        val cameraThread = HandlerThread(cameraThreadNameTitle + id).apply { start() }
        return Handler(cameraThread.looper)
    }
    @SuppressLint("MissingPermission")
    fun openCamera(cameraSurface: CameraSurface): Handler{
        var handler = getCameraHandler(cameraSurface.cameraId)
        cameraSurface.handler = handler
        cameraManager.openCamera(cameraSurface.cameraId, object : CameraDevice.StateCallback(){
            @SuppressLint("WrongConstant")
            override fun onOpened(camera: CameraDevice) {
                cameraSurface.divice = camera
                val targets: List<Surface> = listOf(cameraSurface.surface!!.holder!!.surface)
                var session = createCaptureSession(camera, targets, handler, object :CameraCaptureSession.StateCallback(){

                    override fun onConfigured(session: CameraCaptureSession){
                        Log.i(TAG, "camera ${session.device.id} capture sessio onConfigured")
                        val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply { addTarget(cameraSurface.surface!!.holder.surface) }
                        session.setRepeatingRequest(captureRequest.build(), object : CameraCaptureSession.CaptureCallback() {
                            override fun onCaptureStarted(session: CameraCaptureSession, request: CaptureRequest, timestamp: Long, frameNumber: Long) {
                                super.onCaptureStarted(session, request, timestamp, frameNumber)
                                Log.i(TAG, "camera ${session.device.id} capture started")
                            }
                                                                                                                            }, handler)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        val exc = RuntimeException("Camera ${session.device.id} session configuration failed")
                        Log.e(TAG, exc.message, exc)
                    }
                })
            }

            override fun onDisconnected(camera: CameraDevice) {
                //
            }

            override fun onError(camera: CameraDevice, error: Int) {
                //
            }
        }, handler)
        return handler
    }
    /**
     * Starts a [CameraCaptureSession] and returns the configured session (as the result of the
     * suspend coroutine
     */
    private fun createCaptureSession(
            device: CameraDevice,
            targets: List<Surface>,
            handler: Handler? = null,
            callback: CameraCaptureSession.StateCallback
    ) {

        // Create a capture session using the predefined targets; this also involves defining the
        // session state callback to be notified of when the session is ready
        device.createCaptureSession(targets, callback, handler)
    }
    fun getCharacters(id: String): CameraCharacteristics {
        return cameraManager.getCameraCharacteristics(id)
    }

    companion object {

        private val TAG = CamManager::class.java.simpleName

        private const val IMAGE_BUFFER_SIZE: Int = 3
        /** Helper class used as a data holder for each selectable camera format item */
        private data class FormatItem(val title: String, val cameraId: String, val format: Int)

        /** Helper function used to convert a lens orientation enum into a human-readable string */
        private fun lensOrientationString(value: Int) = when(value) {
            CameraCharacteristics.LENS_FACING_BACK -> "Back"
            CameraCharacteristics.LENS_FACING_FRONT -> "Front"
            CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
            else -> "Unknown"
        }

        /** Helper function used to list all compatible cameras and supported pixel formats */
        @SuppressLint("InlinedApi")
        private fun enumerateCameras(cameraManager: CameraManager): List<String> {
            val availableCameras: MutableList<String> = mutableListOf()

            // Get list of all compatible cameras
            val cameraIds = cameraManager.cameraIdList.filter {
                val characteristics = cameraManager.getCameraCharacteristics(it)
                val capabilities = characteristics.get(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                capabilities?.contains(
                        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE) ?: false
            }


            // Iterate over the list of cameras and return all the compatible ones
            cameraIds.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val orientation = lensOrientationString(
                        characteristics.get(CameraCharacteristics.LENS_FACING)!!)

                // Query the available capabilities and output formats
                val capabilities = characteristics.get(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)!!
                val outputFormats = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.outputFormats

                // All cameras *must* support JPEG output so we don't need to check characteristics
//                availableCameras.add(FormatItem(
//                        "$orientation JPEG ($id)", id, ImageFormat.JPEG))

                // Return cameras that support RAW capability
                if (capabilities.contains(
                                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) &&
                        outputFormats.contains(ImageFormat.RAW_SENSOR)) {
                    availableCameras.add(id)
                }

                // Return cameras that support JPEG DEPTH capability
//                if (capabilities.contains(
//                                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT) &&
//                        outputFormats.contains(ImageFormat.DEPTH_JPEG)) {
//                    availableCameras.add(FormatItem(
//                            "$orientation DEPTH ($id)", id, ImageFormat.DEPTH_JPEG))
//                }
            }

            return availableCameras
        }
    }
}
