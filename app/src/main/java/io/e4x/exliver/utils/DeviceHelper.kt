package io.e4x.exliver.utils

import android.os.Build
import java.security.MessageDigest

object DeviceHelper {
    fun getSN():String{
        return Build.FINGERPRINT
    }
    fun getBrand():String{
        return Build.BRAND
    }
    fun getDeviceID():String{
        var messageDigest = MessageDigest.getInstance("MD5")
        var digest = messageDigest.digest((getBrand() + getSN()).encodeToByteArray())
        var sb : StringBuffer = StringBuffer()
        for (b in digest) {
            var i :Int = b.toInt() and 0xff//获取低八位有效值
            var hexString = Integer.toHexString(i)//将整数转化为16进制
            if (hexString.length < 2) {
                hexString = "0" + hexString//如果是一位的话，补0
            }
            sb.append(hexString)
        }
        return sb.toString()
//        return getSN()
    }
    val deviceUUID = getDeviceID()
}