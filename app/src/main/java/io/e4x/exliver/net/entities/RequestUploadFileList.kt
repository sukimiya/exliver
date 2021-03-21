package io.e4x.exliver.net.entities

import io.e4x.exliver.vo.RecordVO

data class RequestUploadFileList(var deviceID:String, var list: List<RecordVO>) {
}