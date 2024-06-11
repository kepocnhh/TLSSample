package test.cryptographic.tls.provider

import android.util.Log

internal class FinalLoggers : Loggers {
    override fun create(tag: String): Logger {
        return LogcatLogger(tag = tag)
    }
}

private class LogcatLogger(
    private val tag: String,
) : Logger {
    override fun debug(message: String) {
        Log.d(tag, message)
    }
}
