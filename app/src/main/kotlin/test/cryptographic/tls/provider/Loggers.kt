package test.cryptographic.tls.provider

internal interface Loggers {
    fun create(tag: String): Logger
}

internal interface Logger {
    fun debug(message: String)
    fun warning(message: String)
}
