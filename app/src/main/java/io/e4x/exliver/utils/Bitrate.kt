package io.e4x.exliver.utils

class Bitrate {
    companion object {
        const val SUPER_HIGH = 1.0
        const val HIGH = 0.8
        const val MID = 0.6
        private var baseVRate = 2.7126736
        fun videoBitRate(w:Int, h:Int, quality: Double = MID):Int {
            var pixels = w.toDouble() * h.toDouble()
            return Math.floor(pixels * baseVRate * quality).toInt()
        }
    }
}