package io.e4x.exliver.net

import io.e4x.exliver.net.entities.RequestAdd
import io.e4x.exliver.net.entities.RequestUploadFileList
import io.e4x.exliver.net.entities.UploadFileListEvent
import io.e4x.exliver.net.entities.UploadQueryEvent
import io.e4x.exliver.vo.RecordVO
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*


interface IUploadServices {
    @POST("up/add")
    fun add(@Body requestAdd: RequestAdd): Call<UploadResault>
    @POST("up/list")
    fun list(@Body requestUploadFileList: RequestUploadFileList): Call<UploadResault>
    @GET("up/getList")
    fun getList(@Query("deviceId") deviceId:String): Call<UploadFileListEvent>
    @GET("up/getPushURL")
    fun getPushUrl(@Query("host") host:String, @Query("deviceId") deviceId:String): Call<UploadQueryEvent>

    @Multipart
    @POST("up/uploadFile")
    fun upload(@Part file: MultipartBody.Part?, @Query("deviceId") deviceId:String): Call<UploadResault>
    @GET("up/update")
    fun update (@Query("deviceId") deviceId:String): Call<UploadUpdateEvent>

}