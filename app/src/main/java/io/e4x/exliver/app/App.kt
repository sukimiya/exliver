package io.e4x.exliver.app

import android.app.Application

class App: Application() {
//    init {
//        System.loadLibrary("fudx")
//    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object{
        private var instance:Application?=null;
        fun getContext()= instance!!;
    }
}