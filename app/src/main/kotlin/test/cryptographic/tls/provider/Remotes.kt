package test.cryptographic.tls.provider

import kotlin.time.Duration

internal interface Remotes {
    fun hello()
    fun delay(duration: Duration)
}
