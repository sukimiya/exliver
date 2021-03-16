package io.e4x.exliver.net

class NetworkException : Exception() {
    var code: String = "-1"
    var error: String? = ""
    override val message: String?
        get() = error
}