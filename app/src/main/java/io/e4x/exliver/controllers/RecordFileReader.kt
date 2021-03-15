package io.e4x.exliver.controllers

import android.content.ContextWrapper
import android.util.Log
import io.e4x.exliver.utils.FileUtil
import io.e4x.exliver.vo.RecordVO
import java.io.File
import java.io.IOException
import java.util.*

class RecordFileReader(contextWrapper: ContextWrapper) {
    private var contextWrapper = contextWrapper
    private var fileUtil = FileUtil(contextWrapper)
    var recordList: MutableList<RecordVO> = mutableListOf()
    init {
        onStart()
    }
    private fun onStart() {
        var mvlist = fileUtil.getFileList()
        checkOldFiles(mvlist)
        mvlist?.forEach { mv ->
            var fname = mv.name
            if (fname != null) {
                var time = fname.substring(2, fname.lastIndex - 3)
                var recordVO = RecordVO(time, mv.absolutePath)
                recordList.add(recordVO)
            }
        }
        Log.d(TAG, "onStart")
    }

    private fun checkOldFiles(mvlist: Array<File>?) {
        var currentTime = System.currentTimeMillis()
        var minTime = currentTime - STORE_TIMES
//        if(mvlist?.size!! >= MAX_RECORD_FILES) {
            mvlist?.filter {
                var lastModified = it.lastModified()
                lastModified < minTime
            }?.map {
                Log.d(TAG, "delete old file: " + it.absoluteFile)
                it.delete()
            }
//        }
    }
    fun add(recordVO: RecordVO):Boolean {
        return recordList.add(recordVO)
    }
    fun removeOld(){
        var mvlist = fileUtil.getFileList()
        checkOldFiles(mvlist)
    }
    companion object {
        const val TAG = "RecordFileReader"

        const val MAX_RECORD_FILES = 3000
        // 30 day
         const val STORE_TIMES = 2592000000L
        // 1 hour
//        const val STORE_TIMES = 3600000L
    }
}