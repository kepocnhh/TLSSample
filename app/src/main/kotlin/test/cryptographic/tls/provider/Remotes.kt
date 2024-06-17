package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.SessionStartResponse
import java.security.PrivateKey
import java.security.PublicKey
import java.util.UUID
import javax.crypto.SecretKey

internal interface Remotes {
    fun sessionStart(
        publicKey: PublicKey,
        privateKey: PrivateKey,
    ): SessionStartResponse
    fun double(
        secretKey: SecretKey,
        privateKey: PrivateKey,
        publicKey: PublicKey,
        sessionId: UUID,
        number: Int,
    ): Int
}
