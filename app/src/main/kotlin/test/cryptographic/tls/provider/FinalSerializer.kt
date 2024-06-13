package test.cryptographic.tls.provider

import org.json.JSONObject
import test.cryptographic.tls.entity.Keys
import test.cryptographic.tls.entity.SecureConnection
import test.cryptographic.tls.entity.SessionStartRequest
import test.cryptographic.tls.entity.SessionStartResponse
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class FinalSerializer : Serializer {
    override val secureConnection = object : Transformer<SecureConnection, ByteArray> {
        override fun encode(decoded: SecureConnection): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): SecureConnection {
            return JSONObject(String(encoded)).toSecureConnection()
        }
    }

    override val sessionStartRequest = object : Transformer<SessionStartRequest, ByteArray> {
        override fun encode(decoded: SessionStartRequest): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): SessionStartRequest {
            return JSONObject(String(encoded)).toSessionStartRequest()
        }
    }

    override val sessionStartResponse = object : Transformer<SessionStartResponse, ByteArray> {
        override fun encode(decoded: SessionStartResponse): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): SessionStartResponse {
            return JSONObject(String(encoded)).toSessionStartResponse()
        }
    }

    override val keys = object : Transformer<Keys, ByteArray> {
        override fun encode(decoded: Keys): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): Keys {
            return JSONObject(String(encoded)).toKeys()
        }
    }

    companion object {
        private fun SecureConnection.toJSONObject(): JSONObject {
            return JSONObject()
                .put("expires", expires.inWholeMilliseconds)
                .put("sessionId", sessionId.toString())
                .put("publicKey", String(publicKey.encoded))
        }

        private fun JSONObject.toSecureConnection(): SecureConnection {
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = X509EncodedKeySpec(getString("publicKey").toByteArray())
            return SecureConnection(
                sessionId = UUID.fromString(getString("sessionId")),
                publicKey = keyFactory.generatePublic(keySpec),
                expires = getLong("expires").milliseconds,
            )
        }

        private fun SessionStartRequest.toJSONObject(): JSONObject {
            return JSONObject()
                .put("publicKey", String(publicKey.encoded))
        }

        private fun JSONObject.toSessionStartRequest(): SessionStartRequest {
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = X509EncodedKeySpec(getString("publicKey").toByteArray())
            return SessionStartRequest(
                publicKey = keyFactory.generatePublic(keySpec),
            )
        }

        private fun SessionStartResponse.toJSONObject(): JSONObject {
            return JSONObject()
                .put("sessionId", sessionId.toString())
                .put("publicKey", String(publicKey.encoded))
        }

        private fun JSONObject.toSessionStartResponse(): SessionStartResponse {
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = X509EncodedKeySpec(getString("publicKey").toByteArray())
            return SessionStartResponse(
                sessionId = UUID.fromString(getString("sessionId")),
                publicKey = keyFactory.generatePublic(keySpec),
            )
        }

        private fun Keys.toJSONObject(): JSONObject {
            return JSONObject()
                .put("publicKey", String(publicKey.encoded))
                .put("encryptedPrivateKey", String(encryptedPrivateKey))
        }

        private fun JSONObject.toKeys(): Keys {
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = X509EncodedKeySpec(getString("publicKey").toByteArray())
            return Keys(
                publicKey = keyFactory.generatePublic(keySpec),
                encryptedPrivateKey = getString("encryptedPrivateKey").toByteArray(),
            )
        }
    }
}
