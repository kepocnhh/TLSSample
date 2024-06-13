package test.cryptographic.tls.provider

import java.io.InputStream

internal interface Assets {
    fun getAsset(name: String): InputStream
}
