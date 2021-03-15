package io.e4x.exliver.utils

import android.content.ContextWrapper
import android.os.Environment
import java.io.File
import java.io.IOException

class FileUtil(contextWrapper: ContextWrapper) {
    var contextWrapper = contextWrapper
    fun getMVFilePath():String {
        var outputPath = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath + File.separator + STORAGE_FOLDER_NAME
        var folder = File(outputPath)
        if(!folder.exists()) {
            if(!folder.mkdir()){
                throw IOException("folder can't be create")
            }
        }
        var time = System.currentTimeMillis().toString()
        return outputPath + File.separator + "mv" + time + ".mp4"
    }
    fun getFileList(): Array<File>? {
        var filePath = contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DCIM)?.absolutePath + File.separator + STORAGE_FOLDER_NAME
        var folder = File(filePath)
        return folder.listFiles()
    }

    companion object {
        const val STORAGE_FOLDER_NAME = "records"
    }
}