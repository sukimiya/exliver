package io.e4x.exliver.vo

import android.hardware.camera2.CameraDevice
import android.os.Handler
import io.e4x.exliver.views.AutoFitSurfaceView

class CameraSurface(surface: AutoFitSurfaceView?, cameraId:String) {
    var surface: AutoFitSurfaceView? = surface
    var cameraId: String = cameraId
    var isSelected: Boolean = false
    var handler: Handler? = null
    var divice: CameraDevice? = null
}