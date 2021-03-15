package io.e4x.exliver.controllers

import android.util.Log
import io.e4x.exliver.vo.RecordVO

class RecordUploader {
    fun upload(recordVO: RecordVO) {
        Log.d(TAG, "uploading record [mv${recordVO.time}]")
    }
    companion object {
        private var TAG = RecordUploader::class.java.simpleName
    }
}