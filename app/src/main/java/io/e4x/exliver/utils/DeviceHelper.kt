package io.e4x.exliver.utils

import android.Manifest.permission.READ_PHONE_STATE
import android.annotation.SuppressLint
import android.app.Service
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.telephony.TelephonyManager
import android.util.Log
import androidx.annotation.RequiresPermission
import io.e4x.exliver.MainActivity
import java.io.*
import java.lang.Exception
import java.security.MessageDigest
import java.util.*


object DeviceHelper {
    @SuppressLint("MissingPermission")
    fun getAndroidId(): String {
        if (deviceIdHolder == null) {
            var folder = MainActivity.theContext?.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            if (folder?.exists()!!){
                var idfile = File(folder.absolutePath + File.separator + ID_STORE_FILE)
                if(idfile?.exists()) {
                    val fileReader = InputStreamReader(FileInputStream(idfile), "utf-8")
                    val fp = BufferedReader(fileReader)
                    var line: String? = null
                    var message = ""
                    while (fp.readLine().also { line = it } != null) {
                        message += line;
                    }
                    fp.close()
                    if (message != null && message != "") {
                        deviceIdHolder = message
                        Log.d("DeviceHelper", "get deviceId:$deviceIdHolder")
                    }
                } else {
                    generateDeviceId(folder)
                }
            } else {
                generateDeviceId(folder)
            }
        }
        return deviceIdHolder!!
    }
    @SuppressLint("HardwareIds", "SupportAnnotationUsage")
    @RequiresPermission(READ_PHONE_STATE)
    fun generateDeviceId(folder: File) {
        if (folder?.exists()!!) {
            folder.mkdir()
        }
        var tm:TelephonyManager = MainActivity.theContext?.getSystemService(Service.TELEPHONY_SERVICE) as TelephonyManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                var androidId = Settings.Secure.getString(MainActivity.theContext?.contentResolver, Settings.Secure.ANDROID_ID).replace("\n","")
                deviceIdHolder = androidId
                Log.d("DeviceHelper", "generated serial + deviceId:$deviceIdHolder")
            } catch (e:Exception) {
                e.printStackTrace()
                deviceIdHolder = UUID.randomUUID().toString()
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            deviceIdHolder = tm.imei
        } else {
            deviceIdHolder = tm.getDeviceId();
        }
        var saveFile = File(folder.absolutePath + File.separator + ID_STORE_FILE)
        val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(saveFile, false), "UTF-8"))
        try {
            var saveFile = File(folder.absolutePath + File.separator + ID_STORE_FILE)
            val writer = BufferedWriter(OutputStreamWriter(FileOutputStream(saveFile, false), "UTF-8"))
            writer.write(deviceIdHolder)
            writer.flush()
            writer.close()
            Log.d("DeviceHelper", "generated deviceId:$deviceIdHolder")
        } catch (e:IOException) {
            e.printStackTrace()
        }
    }
    private var deviceIdHolder: String? = null
    val deviceUUID = getAndroidId()
    const val ID_STORE_FILE = "deviceid.json"
}