package test.cryptographic.tls.provider

import org.json.JSONObject
import test.cryptographic.tls.entity.Keys
import test.cryptographic.tls.entity.SecureConnection
import test.cryptographic.tls.entity.SessionStartRequest
import test.cryptographic.tls.entity.SessionStartResponse
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds

internal class FinalSerializer(
    private val secrets: Secrets,
) : Serializer {
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

    private fun Keys.toJSONObject(): JSONObject {
        return JSONObject()
            .put("publicKey", secrets.base64(publicKey.encoded))
            .put("encryptedPrivateKey", secrets.base64(encryptedPrivateKey))
    }

    private fun JSONObject.toKeys(): Keys {
        return Keys(
            publicKey = secrets.toPublicKey(secrets.base64(getString("publicKey"))),
            encryptedPrivateKey = secrets.base64(getString("encryptedPrivateKey")),
        )
    }

    private fun SessionStartRequest.toJSONObject(): JSONObject {
        return JSONObject()
            .put("publicKey", secrets.base64(publicKey.encoded))
    }

    private fun JSONObject.toSessionStartRequest(): SessionStartRequest {
        return SessionStartRequest(
            publicKey = secrets.toPublicKey(secrets.base64(getString("publicKey"))),
        )
    }

    private fun SessionStartResponse.toJSONObject(): JSONObject {
        return JSONObject()
            .put("sessionId", sessionId.toString())
            .put("publicKey", secrets.base64(publicKey.encoded))
    }

    private fun JSONObject.toSessionStartResponse(): SessionStartResponse {
        return SessionStartResponse(
            sessionId = UUID.fromString(getString("sessionId")),
            publicKey = secrets.toPublicKey(secrets.base64(getString("publicKey"))),
        )
    }

    private fun SecureConnection.toJSONObject(): JSONObject {
        return JSONObject()
            .put("expires", expires.inWholeMilliseconds)
            .put("sessionId", sessionId.toString())
            .put("publicKey", secrets.base64(publicKey.encoded))
    }

    private fun JSONObject.toSecureConnection(): SecureConnection {
        return SecureConnection(
            sessionId = UUID.fromString(getString("sessionId")),
            publicKey = secrets.toPublicKey(secrets.base64(getString("publicKey"))),
            expires = getLong("expires").milliseconds,
        )
    }
}
