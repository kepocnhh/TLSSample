package test.cryptographic.tls.provider

import java.security.PublicKey
import javax.crypto.SecretKey

internal interface Secrets {
    fun getSecretKey(password: String): SecretKey
    fun toPublicKey(encoded: ByteArray): PublicKey
    fun encrypt(secretKey: SecretKey, decrypted: ByteArray): ByteArray
    fun hash(bytes: ByteArray): String
    fun base64(encoded: String): ByteArray
    fun base64(decoded: ByteArray): String
}
