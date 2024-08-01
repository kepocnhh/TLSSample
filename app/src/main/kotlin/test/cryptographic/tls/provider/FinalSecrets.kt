package test.cryptographic.tls.provider

import java.security.KeyFactory
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

internal class FinalSecrets : Secrets {
    override fun getSecretKey(password: String): SecretKey {
        val keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val salt = "salt".toByteArray()
        val keySpec = PBEKeySpec(password.toCharArray(), salt, 1, 256)
        return keyFactory.generateSecret(keySpec)
    }

    override fun toPublicKey(encoded: ByteArray): PublicKey {
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = X509EncodedKeySpec(encoded)
        return keyFactory.generatePublic(keySpec)
    }

    override fun toPrivateKey(encoded: ByteArray): PrivateKey {
        val keyFactory = KeyFactory.getInstance("RSA")
        val keySpec = PKCS8EncodedKeySpec(encoded)
        return keyFactory.generatePrivate(keySpec)
    }

    override fun toSecretKey(encoded: ByteArray): SecretKey {
        return SecretKeySpec(encoded, "AES")
    }

    override fun newSecretKey(): SecretKey {
        val generator = KeyGenerator.getInstance("AES")
        return generator.generateKey()
    }

    override fun encrypt(secretKey: SecretKey, decrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        return cipher.doFinal(decrypted)
    }

    override fun decrypt(secretKey: SecretKey, encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        return cipher.doFinal(encrypted)
    }

    override fun encrypt(publicKey: PublicKey, decrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        return cipher.doFinal(decrypted)
    }

    override fun decrypt(privateKey: PrivateKey, encrypted: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        return cipher.doFinal(encrypted)
    }

    override fun sign(privateKey: PrivateKey, payload: ByteArray): ByteArray {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initSign(privateKey)
        signature.update(payload)
        return signature.sign()
    }

    override fun verify(publicKey: PublicKey, message: ByteArray, sig: ByteArray): Boolean {
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(publicKey)
        signature.update(message)
        return signature.verify(sig)
    }

    override fun hash(bytes: ByteArray): String {
        val md = MessageDigest.getInstance("SHA256")
        return md.digest(bytes).joinToString(separator = "") { String.format("%02x", it.toInt() and 0xff) }
    }

    override fun base64(encoded: String): ByteArray {
        return Base64.getDecoder().decode(encoded.toByteArray())
    }

    override fun base64(decoded: ByteArray): String {
        return Base64.getEncoder().encodeToString(decoded)
    }
}
