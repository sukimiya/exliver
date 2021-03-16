package io.e4x.exliver.net

import android.annotation.SuppressLint
import android.util.Log
import io.e4x.exliver.net.entities.RequestUploadFileList
import io.e4x.exliver.vo.RecordVO
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class UploadServices {
    private val retrofit = Retrofit.Builder().baseUrl(uploadUrl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val uploadServices = retrofit.create(IUploadServices::class.java)

    @SuppressLint("CheckResult")
    fun add(recordVO: RecordVO) {
        uploadServices.add(recordVO).rx().doOnError(){
            it.printStackTrace()
        }.subscribe(){
            Log.d(TAG, "code:" + it.code.toString() + " message:" + it.message)
        }
    }
    @SuppressLint("CheckResult")
    fun setList(list: List<RecordVO>) {
        uploadServices.list(RequestUploadFileList(list)).rx().doOnError(){
            it.printStackTrace()
        }.subscribe(){
            Log.d(TAG, "code:" + it.code.toString() + " message:" + it.message)
        }
    }
    fun getList(): List<RecordVO> {
        return uploadServices.getList().executeBody().list
    }
    fun getUpstreanUrl(): String {
        return uploadServices.getPushUrl().executeBody().resault
    }
    companion object {
        const val TAG = "UploadServices"
        var uploadUrl:String = "http://192.168.31.61:5089/"

        private var instance:UploadServices? = null
        fun getInstance():UploadServices{
            if(instance == null) {
                instance = UploadServices()
            }
            return instance!!
        }
    }
}