package test.cryptographic.tls.provider

import sp.kx.http.TLSEnvironment
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import java.util.UUID
import javax.crypto.SecretKey
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class FinalTLSEnvironment(
    private val locals: Locals,
    private val sessions: Sessions,
    private val secrets: Secrets,
) : TLSEnvironment {
    override val timeMax = 1.minutes

    override val keyPair: KeyPair get() {
        val keys = locals.keys ?: error("No keys!")
        return KeyPair(
            keys.publicKey,
            sessions.privateKey ?: error("No private key!"),
        )
    }

    override fun decrypt(key: PrivateKey, encrypted: ByteArray): ByteArray {
        return secrets.decrypt(key, encrypted)
    }

    override fun decrypt(key: SecretKey, encrypted: ByteArray): ByteArray {
        return secrets.decrypt(key, encrypted)
    }

    override fun encrypt(key: PublicKey, decrypted: ByteArray): ByteArray {
        return secrets.encrypt(key, decrypted)
    }

    override fun encrypt(key: SecretKey, decrypted: ByteArray): ByteArray {
        return secrets.encrypt(key, decrypted)
    }

    override fun newSecretKey(): SecretKey {
        return secrets.newSecretKey()
    }

    override fun newUUID(): UUID {
        return UUID.randomUUID()
    }

    override fun now(): Duration {
        return System.currentTimeMillis().milliseconds
    }

    override fun sign(key: PrivateKey, encoded: ByteArray): ByteArray {
        return secrets.sign(key, encoded)
    }

    override fun toSecretKey(encoded: ByteArray): SecretKey {
        return secrets.toSecretKey(encoded)
    }

    override fun verify(key: PublicKey, encoded: ByteArray, signature: ByteArray): Boolean {
        return secrets.verify(key, encoded, signature)
    }
}
