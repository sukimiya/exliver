package io.e4x.exliver.net

import android.annotation.SuppressLint
import android.util.Log
import io.e4x.exliver.net.entities.RequestAdd
import io.e4x.exliver.net.entities.RequestUploadFileList
import io.e4x.exliver.net.entities.UploadQueryEvent
import io.e4x.exliver.utils.DeviceHelper
import io.e4x.exliver.vo.RecordVO
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import java.io.File
import java.util.*

class UploadServices {
    private var timer:Timer = Timer(true)
    private val retrofit = Retrofit.Builder().baseUrl(uploadUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val uploadServices = retrofit.create(IUploadServices::class.java)
    init {
        startLisener()
    }

    @SuppressLint("CheckResult")
    fun add(recordVO: RecordVO) {
        uploadServices.add(RequestAdd(DeviceHelper.deviceUUID, recordVO)).rx().doOnError(){
            it.printStackTrace()
        }.subscribe(){
            Log.d(TAG, "code:" + it.code.toString() + " message:" + it.message)
        }
    }
    @SuppressLint("CheckResult")
    fun setList(list: List<RecordVO>) {
        uploadServices.list(RequestUploadFileList(DeviceHelper.deviceUUID, list)).rx().doOnError(){
            it.printStackTrace()
        }.subscribe(){
            Log.d(TAG, "code:" + it.code.toString() + " message:" + it.message)
        }
    }
    fun getList(): List<RecordVO> {
        return uploadServices.getList(DeviceHelper.deviceUUID).executeBody().list
    }
    fun getUpstreanUrl(): String {
        return uploadServices.getPushUrl(uploadDomain, DeviceHelper.deviceUUID).executeBody().resault
    }
    fun getPushUrl(): Call<UploadQueryEvent> {
        return uploadServices.getPushUrl(uploadDomain, DeviceHelper.deviceUUID)
    }
    fun update():UploadUpdateEvent {
        var deviceId = DeviceHelper.getDeviceID()
        return uploadServices.update(deviceId).executeBody()
    }
    @SuppressLint("CheckResult")
    fun upload(file:String, fileObj:File) {
        var requestBody = RequestBody.create(MediaType.parse("video/mpeg"), fileObj)
        var part = MultipartBody.Part.createFormData("file", file, requestBody)
        uploadServices.upload(part, DeviceHelper.deviceUUID).rx().subscribe(){
            if (it.code != 0) {
                var e = NetworkException()
                e.code = "-1"
                e.error = it.message
                throw e
            }

        }
    }
    private var startUploading = false
    fun startLisener() {
        timer.scheduleAtFixedRate(object:TimerTask(){
            override fun run() {
                if (startUploading) {
                    return
                }
                var updateResult = update()
                if(updateResult.data != null && updateResult.data != ""){
                    var file = File(updateResult.data)
                    if(file.exists()) {
                        startUploading = true
                        Log.d(TAG, "try upload" + file.absoluteFile)
                        try {
                            upload(file.name, file)
                        } catch (e:NetworkException) {
                            startUploading = false
                            Log.d(TAG, "upload fail:" + e.error)
                        }
                        Log.d(TAG,  "upload finish:" + file.absoluteFile)
                        startUploading = false
                    } else {
                        Log.d(TAG, "the file not exists")
                    }
                }
            }
        },0L, 3000L)
    }
    companion object {
        const val TAG = "UploadServices"
        var uploadUrl:String = "http://e4x.live:5089/"
//        var uploadUrl:String = "http://192.168.31.240:5089/"
//        var uploadDomain:String = "192.168.31.240"
        var uploadDomain:String = "e4x.live"

        private var instance:UploadServices? = null
        fun getInstance():UploadServices{
            if(instance == null) {
                instance = UploadServices()
            }
            return instance!!
        }
    }
}