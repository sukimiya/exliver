package io.e4x.exliver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.android.camera.utils.getPreviewOutputSize
import io.e4x.exliver.controllers.CamManager
import io.e4x.exliver.vo.CameraSurface


private const val PERMISSIONS_REQUEST_CODE = 10
private val PERMISSIONS_REQUIRED = arrayOf(Manifest.permission.CAMERA)
/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = requireContext().applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }
    lateinit var camManager: CamManager
    val cameraList: List<CameraSurface> by lazy {
        listOf(CameraSurface(null, "0"),
            CameraSurface(null, "1"),
            CameraSurface(null, "2"),
            CameraSurface(null, "3"))
    }
    var selected: MutableList<CameraSurface> = listOf(cameraList[0], cameraList[1]).toMutableList()
    var availableCameras: MutableList<CameraSurface>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (hasPermissions(requireContext())) {
            // If permissions have already been granted, proceed
        } else {
            // Request camera-related permissions
            requestPermissions(PERMISSIONS_REQUIRED, PERMISSIONS_REQUEST_CODE)
        }
    }
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }
    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Takes the user to the success fragment when permission is granted
            } else {
                Toast.makeText(context, "Permission request denied", Toast.LENGTH_LONG).show()
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.button_first).setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        cameraList.map { cameraSurface ->
            when (cameraSurface.cameraId) {
                "0" -> {
                    cameraSurface.surface = view.findViewById(R.id.surfaceView1)
                }
                "1" -> {
                    cameraSurface.surface = view.findViewById(R.id.surfaceView2)
                }
                "2" -> {
                    cameraSurface.surface = view.findViewById(R.id.surfaceView3)
                }
                "3" -> {
                    cameraSurface.surface = view.findViewById(R.id.surfaceView4)
                }
            }
        }
        camManager = CamManager(cameraManager)
        availableCameras = camManager.checkAvalibleCameras(cameraList)
        availableCameras?.map {
            Log.i(TAG, "Success get avalible camera:" + it.cameraId)
            it.surface!!.holder!!.addCallback(object : SurfaceHolder.Callback{
                override fun surfaceCreated(holder: SurfaceHolder) {
                    val previewSize = getPreviewOutputSize(
                            it.surface!!.display, camManager.getCharacters(it.cameraId), SurfaceHolder::class.java)
                    Log.d(TAG, "View finder size: ${it.surface!!.width} x ${it.surface!!.height}")
                    Log.d(TAG, "Selected preview size: $previewSize")
                    it.surface!!.setAspectRatio(previewSize.width, previewSize.height)
                    camManager.openCamera(it)
                }

                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                    //
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {
                    //
                }

            })
        }
    }
    companion object {
        private val TAG = FirstFragment::class.java.simpleName

        /** Convenience method used to check if all permissions required by this app are granted */
        fun hasPermissions(context: Context) = PERMISSIONS_REQUIRED.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}