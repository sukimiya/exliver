package io.e4x.exliver.net

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import java.io.IOException
import java.io.InterruptedIOException

fun <T> Call<T>.rx(): Observable<T> {
    return Observable.create<T> {
        try {
            it.onNext(executeBody())
            it.onComplete()
        } catch (e: Exception) {
            it.onError(e)
        }
    }.doOnError {
        it.stackTrace.map {
            System.err.println(it.toString())
        }
    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun <T> Call<T>.executeBody(): T {
    try {
        val response = execute()
        if (response.isSuccessful) {
            return response.body()!!
        } else if (response.errorBody() != null) {
            val text = response.errorBody()!!.string()
            val json = JSONObject(text)
            val code = json.getString("code")
            val error = json.getString("message")
            val exception = NetworkException()
            exception.code = code
            exception.error = error
            throw exception
        } else {
            throw Exception("未知错误")
        }
    } catch (e: Exception) {
        if (e !is NetworkException) {
            when (e) {
                is InterruptedIOException -> throw Exception("服务请求超时，请稍后重试")
                is JSONException -> throw Exception("服务器开小差，请稍后重试")
                is IOException -> throw Exception("当前网络不给力，请稍后再试")
                else -> throw Exception("未知错误")
            }
        }
        throw e
    }
}