package test.cryptographic.tls.provider

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.SecretKey

internal interface Secrets {
    fun getSecretKey(password: String): SecretKey
    fun toPublicKey(encoded: ByteArray): PublicKey
    fun toPrivateKey(encoded: ByteArray): PrivateKey
    fun toSecretKey(encoded: ByteArray): SecretKey
    fun newSecretKey(): SecretKey
    fun encrypt(secretKey: SecretKey, decrypted: ByteArray): ByteArray
    fun decrypt(secretKey: SecretKey, encrypted: ByteArray): ByteArray
    fun encrypt(publicKey: PublicKey, decrypted: ByteArray): ByteArray
    fun decrypt(privateKey: PrivateKey, encrypted: ByteArray): ByteArray
    fun hash(bytes: ByteArray): String
    fun base64(encoded: String): ByteArray
    fun base64(decoded: ByteArray): String
    fun sign(privateKey: PrivateKey, payload: ByteArray): ByteArray
    fun verify(publicKey: PublicKey, message: ByteArray, sig: ByteArray): Boolean
}
