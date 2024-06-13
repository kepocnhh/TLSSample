package test.cryptographic.tls.entity

import java.security.PublicKey

internal class Keys(
    val publicKey: PublicKey,
    val encryptedPrivateKey: ByteArray,
)
