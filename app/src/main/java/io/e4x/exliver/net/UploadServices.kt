package io.e4x.exliver.net

import android.annotation.SuppressLint
import android.content.ContextWrapper
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
import java.io.File
import java.util.*

class UploadServices(theContextWapper: ContextWrapper) {
    private var timer:Timer = Timer(true)
    private val retrofit = Retrofit.Builder().baseUrl(uploadUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val uploadServices = retrofit.create(IUploadServices::class.java)
    private lateinit var deviceHelper:DeviceHelper
    init {
        deviceHelper = DeviceHelper(theContextWapper)
        startLisener()
    }
    @SuppressLint("CheckResult")
    fun add(recordVO: RecordVO) {
        uploadServices.add(RequestAdd(deviceHelper.deviceUUID, recordVO)).rx().doOnError(){
            it.printStackTrace()
        }.subscribe(){
            Log.d(TAG, "code:" + it.code.toString() + " message:" + it.message)
        }
    }
    @SuppressLint("CheckResult")
    fun setList(list: List<RecordVO>) {
        uploadServices.list(RequestUploadFileList(deviceHelper.deviceUUID, list)).rx().doOnError(){
            it.printStackTrace()
        }.subscribe(){
            Log.d(TAG, "code:" + it.code.toString() + " message:" + it.message)
        }
    }
    fun getList(): List<RecordVO> {
        return uploadServices.getList(deviceHelper.deviceUUID).executeBody().list
    }
    fun getUpstreanUrl(): String {
        return uploadServices.getPushUrl(uploadDomain, deviceHelper.deviceUUID).executeBody().resault
    }
    fun getPushUrl(): Call<UploadQueryEvent> {
        return uploadServices.getPushUrl(uploadDomain, deviceHelper.deviceUUID)
    }
    fun update():UploadUpdateEvent {
        var deviceId = deviceHelper.deviceUUID
        try {
            return uploadServices.update(deviceId).executeBody()
        } catch (e:Exception) {
            e.printStackTrace()
        }
        return UploadUpdateEvent("", null)
    }
    @SuppressLint("CheckResult")
    fun upload(file:String, fileObj:File) {
        var requestBody = RequestBody.create(MediaType.parse("video/mpeg"), fileObj)
        var part = MultipartBody.Part.createFormData("file", file, requestBody)
        uploadServices.upload(part, deviceHelper.deviceUUID).rx().subscribe(){
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
                try {
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
                } catch (e:Exception) {
                    e.printStackTrace()
                    Log.d(TAG, "update connect error")
                }
            }
        },0L, 3000L)
    }
    companion object {
        const val TAG = "UploadServices"
        var uploadUrl:String = "http://e4x.live:5089/"
        var uploadDomain:String = "e4x.live"
//        var uploadUrl:String = "http://192.168.191.128:5089/"
//        var uploadDomain:String = "192.168.191.128"

        private var instance:UploadServices? = null
        fun getInstance(theContextWapper:ContextWrapper):UploadServices{
            if(instance == null) {
                instance = UploadServices(theContextWapper)
            }
            return instance!!
        }
    }
}