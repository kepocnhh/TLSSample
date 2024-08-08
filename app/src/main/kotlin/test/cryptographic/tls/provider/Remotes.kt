package test.cryptographic.tls.provider

import java.security.PrivateKey
import java.security.PublicKey

internal interface Remotes {
    fun double(
        publicKey: PublicKey,
        privateKey: PrivateKey,
        number: Int,
    ): Int
}
