package test.cryptographic.tls.module.receiver

import sp.kx.http.HttpRequest
import sp.kx.http.HttpResponse
import sp.kx.http.HttpRouting
import test.cryptographic.tls.BuildConfig
import test.cryptographic.tls.entity.SecureConnection
import test.cryptographic.tls.entity.Session
import test.cryptographic.tls.entity.SessionStartResponse
import test.cryptographic.tls.module.app.Injection
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.util.Date
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

internal class ReceiverRouting(
    private val injection: Injection,
) : HttpRouting {
    private val logger = injection.loggers.create("[Receiver|Routing]")

    private val mapping = mapOf(
        "/version" to mapOf(
            "GET" to ::onGetVersion,
        ),
        "/double" to mapOf(
            "POST" to ::onPostDouble,
        ),
        "/session/start" to mapOf(
            "POST" to ::onPostSessionStart,
        ),
    )

    private fun onPostSessionStart(request: HttpRequest): HttpResponse {
        val oldConnection = injection.sessions.secureConnection
        val now = System.currentTimeMillis().milliseconds
        if (oldConnection != null) {
            if (oldConnection.expires > now) {
                return HttpResponse(
                    version = "1.1",
                    code = 500,
                    message = "Internal Server Error",
                    headers = emptyMap(),
                    body = "todo".toByteArray(),
                )
            }
            injection.sessions.secureConnection = null
        }
        val body = request.body ?: return HttpResponse(
            version = "1.1",
            code = 500,
            message = "Internal Server Error",
            headers = emptyMap(),
            body = "todo".toByteArray(),
        )
        val publicKey = injection.serializer.sessionStartRequest.decode(body).publicKey
        logger.debug("transmitter:public:key: ${injection.secrets.hash(publicKey.encoded)}")
        val sessionId = UUID.randomUUID()
        val secretKey = injection.secrets.newSecretKey()
        injection.sessions.secureConnection = SecureConnection(
            sessionId = sessionId,
            expires = now + 1.minutes,
            secretKey = secretKey,
        )
        logger.debug("session:expires: ${Date(injection.sessions.secureConnection!!.expires.inWholeMilliseconds)}") // todo
        logger.debug("session:ID: $sessionId")
        val session = Session(id = sessionId)
        logger.debug("secret:key: ${injection.secrets.hash(secretKey.encoded)}")
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = mapOf(
                "secretKey" to injection.secrets.base64(injection.secrets.encrypt(publicKey, secretKey.encoded)),
                "session" to injection.secrets.base64(injection.secrets.encrypt(secretKey, injection.serializer.session.encode(session))),
            ),
            body = null,
        )
    }

    private fun onPostDouble(request: HttpRequest): HttpResponse {
        logger.debug("on post double...")
        val secureConnection = injection.sessions.secureConnection
        if (secureConnection == null) {
            return HttpResponse(
                version = "1.1",
                code = 500,
                message = "Internal Server Error",
                headers = mapOf(
                    "message" to "No session!",
                ),
                body = "todo".toByteArray(),
            )
        }
        val now = System.currentTimeMillis().milliseconds
        logger.debug("session:expires: ${Date(secureConnection.expires.inWholeMilliseconds)}")
        if (secureConnection.expires < now) {
            return HttpResponse(
                version = "1.1",
                code = 500,
                message = "Internal Server Error",
                headers = mapOf(
                    "message" to "Session is expired!",
                ),
                body = "todo".toByteArray(),
            )
        }
        val body = request.body ?: error("No body!")
        val decrypted = injection.secrets.decrypt(secureConnection.secretKey, body)
        logger.debug("secret:key: ${injection.secrets.hash(secureConnection.secretKey.encoded)}")
        val decryptedRequest = injection.serializer.decryptedRequest(injection.serializer.ints).decode(decrypted)
        logger.debug("transmitter:session:ID: ${decryptedRequest.session.id}")
        if (decryptedRequest.session.id != secureConnection.sessionId) {
            return HttpResponse(
                version = "1.1",
                code = 500,
                message = "Internal Server Error",
                headers = mapOf(
                    "message" to "Session ID error!",
                ),
                body = "todo".toByteArray(),
            )
        }
        // todo signature
        val number = decryptedRequest.payload
        if (number !in 1..128) TODO()
        injection.sessions.secureConnection = null
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = emptyMap(),
            body = injection.secrets.encrypt(
                secretKey = secureConnection.secretKey,
                decrypted = injection.serializer.ints.encode(number * 2),
            ),
        )
    }

    private fun onGetVersion(request: HttpRequest): HttpResponse {
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = emptyMap(),
            body = "${BuildConfig.VERSION_NAME}-${BuildConfig.VERSION_CODE}".toByteArray(),
        )
    }

    override fun route(request: HttpRequest): HttpResponse {
        logger.debug(
            message = """
                <-- request
                ${request.method} ${request.query}
                headers: ${request.headers}
            """.trimIndent(),
        )
        val route = mapping[request.query] ?: return HttpResponse(
            version = "1.1",
            code = 404,
            message = "Not Found",
            headers = emptyMap(),
            body = null,
        )
        val transform = route[request.method] ?: return HttpResponse(
            version = "1.1",
            code = 405,
            message = "Method Not Allowed",
            headers = emptyMap(),
            body = null,
        )
        val response = transform(request)
        logger.debug(
            message = """
                --> response
                ${response.code} ${response.message}
                headers: ${response.headers}
            """.trimIndent(),
        )
        return response
    }
}
