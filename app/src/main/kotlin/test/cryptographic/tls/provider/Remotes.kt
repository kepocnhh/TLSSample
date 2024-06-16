package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.SessionStartRequest
import test.cryptographic.tls.entity.Session
import test.cryptographic.tls.entity.SessionStartResponse
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

internal interface Remotes {
    fun version(): String
    fun sessionStart(privateKey: PrivateKey, request: SessionStartRequest): SessionStartResponse
    fun encrypted(
        secretKey: SecretKey,
        session: Session,
    ): EncryptedRemotes

    fun sessionStart(publicKey: PublicKey): ByteArray
    fun double(encrypted: ByteArray): ByteArray
}
