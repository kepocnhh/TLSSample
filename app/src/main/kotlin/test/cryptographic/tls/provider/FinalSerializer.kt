package test.cryptographic.tls.provider

import org.json.JSONObject
import test.cryptographic.tls.entity.DecryptedRequest
import test.cryptographic.tls.entity.Keys
import test.cryptographic.tls.entity.SecureConnection
import test.cryptographic.tls.entity.Session
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

    override val session = object : Transformer<Session, ByteArray> {
        override fun encode(decoded: Session): ByteArray {
            return decoded.toJSONObject().toString().toByteArray()
        }

        override fun decode(encoded: ByteArray): Session {
            return JSONObject(String(encoded)).toSession()
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

    override val ints = object : Transformer<Int, ByteArray> {
        override fun encode(decoded: Int): ByteArray {
            return "$decoded".toByteArray()
        }

        override fun decode(encoded: ByteArray): Int {
            return String(encoded).toInt()
        }
    }

    override fun <T : Any> decryptedRequest(transformer: Transformer<T, ByteArray>): Transformer<DecryptedRequest<T>, ByteArray> {
        return object : Transformer<DecryptedRequest<T>, ByteArray> {
            override fun encode(decoded: DecryptedRequest<T>): ByteArray {
                return decoded.toJSONObject(transformer).toString().toByteArray()
            }

            override fun decode(encoded: ByteArray): DecryptedRequest<T> {
                return JSONObject(String(encoded)).toDecryptedRequest(transformer)
            }
        }
    }

    private fun Session.toJSONObject(): JSONObject {
        return JSONObject()
            .put("id", id.toString())
    }

    private fun JSONObject.toSession(): Session {
        return Session(
            id = UUID.fromString(getString("id")),
        )
    }

    private fun <T : Any> JSONObject.toDecryptedRequest(transformer: Transformer<T, ByteArray>): DecryptedRequest<T> {
        return DecryptedRequest(
            session = getJSONObject("session").toSession(),
            payload = transformer.decode(secrets.base64(getString("payload"))),
        )
    }

    private fun <T : Any> DecryptedRequest<T>.toJSONObject(transformer: Transformer<T, ByteArray>): JSONObject {
        return JSONObject()
            .put("session", session.toJSONObject())
            .put("payload", secrets.base64(transformer.encode(payload)))
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
            .put("session", session.toJSONObject())
            .put("secretKey", secrets.base64(secretKey.encoded))
    }

    private fun JSONObject.toSessionStartResponse(): SessionStartResponse {
        return SessionStartResponse(
            session = getJSONObject("session").toSession(),
            secretKey = secrets.toSecretKey(secrets.base64(getString("secretKey"))),
        )
    }

    private fun SecureConnection.toJSONObject(): JSONObject {
        return JSONObject()
            .put("expires", expires.inWholeMilliseconds)
            .put("sessionId", sessionId.toString())
            .put("secretKey", secrets.base64(secretKey.encoded))
    }

    private fun JSONObject.toSecureConnection(): SecureConnection {
        return SecureConnection(
            sessionId = UUID.fromString(getString("sessionId")),
            secretKey = secrets.toSecretKey(secrets.base64(getString("secretKey"))),
            expires = getLong("expires").milliseconds,
        )
    }
}
