package test.cryptographic.tls.provider

import org.json.JSONObject
import test.cryptographic.tls.entity.SecureConnection
import test.cryptographic.tls.entity.StartSessionRequest
import test.cryptographic.tls.entity.StartSessionResponse
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

    override val startSessionRequest = object : Transformer<StartSessionRequest, ByteArray> {
        override fun encode(decoded: StartSessionRequest): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): StartSessionRequest {
            return JSONObject(String(encoded)).toStartSessionRequest()
        }
    }

    override val startSessionResponse = object : Transformer<StartSessionResponse, ByteArray> {
        override fun encode(decoded: StartSessionResponse): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): StartSessionResponse {
            return JSONObject(String(encoded)).toStartSessionResponse()
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

        private fun StartSessionRequest.toJSONObject(): JSONObject {
            return JSONObject()
                .put("publicKey", String(publicKey.encoded))
        }

        private fun JSONObject.toStartSessionRequest(): StartSessionRequest {
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = X509EncodedKeySpec(getString("publicKey").toByteArray())
            return StartSessionRequest(
                publicKey = keyFactory.generatePublic(keySpec),
            )
        }

        private fun StartSessionResponse.toJSONObject(): JSONObject {
            return JSONObject()
                .put("sessionId", sessionId.toString())
                .put("publicKey", String(publicKey.encoded))
        }

        private fun JSONObject.toStartSessionResponse(): StartSessionResponse {
            val keyFactory = KeyFactory.getInstance("RSA")
            val keySpec = X509EncodedKeySpec(getString("publicKey").toByteArray())
            return StartSessionResponse(
                sessionId = UUID.fromString(getString("sessionId")),
                publicKey = keyFactory.generatePublic(keySpec),
            )
        }
    }
}
