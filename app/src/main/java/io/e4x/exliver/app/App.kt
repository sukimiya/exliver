package io.e4x.exliver.app

import android.app.Application

class App: Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    companion object{
        private var instance:Application?=null;
        fun getContext()= instance!!;
    }
}