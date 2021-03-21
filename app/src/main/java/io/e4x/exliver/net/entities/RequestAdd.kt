package io.e4x.exliver.net.entities

import io.e4x.exliver.vo.RecordVO

data class RequestAdd(var deviceId:String, var recordVo:RecordVO) {
}