package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.SessionStartRequest
import kotlin.time.Duration

internal interface Remotes {
    fun hello()
    fun delay(duration: Duration)
    fun double(number: Int): Int
    fun sessionStart(request: SessionStartRequest): ByteArray
}
