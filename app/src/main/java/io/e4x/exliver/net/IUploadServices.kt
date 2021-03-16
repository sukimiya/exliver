package io.e4x.exliver.net

import io.e4x.exliver.net.entities.RequestUploadFileList
import io.e4x.exliver.net.entities.UploadFileListEvent
import io.e4x.exliver.net.entities.UploadQueryEvent
import io.e4x.exliver.vo.RecordVO
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IUploadServices {
    @POST("up/add")
    fun add(@Body recordVO: RecordVO): Call<UploadResault>
    @POST("up/list")
    fun list(@Body requestUploadFileList:RequestUploadFileList): Call<UploadResault>
    @GET("up/getList")
    fun getList(): Call<UploadFileListEvent>
    @GET("up/getPushURL")
    fun getPushUrl(): Call<UploadQueryEvent>
}