package test.cryptographic.tls.module.receiver

import sp.kx.http.HttpRequest
import sp.kx.http.HttpResponse
import sp.kx.http.HttpRouting
import test.cryptographic.tls.BuildConfig
import test.cryptographic.tls.entity.SecureConnection
import test.cryptographic.tls.module.app.Injection
import java.util.Date
import java.util.UUID
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

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
        logger.debug("on post session start...")
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
        val keys = injection.locals.keys ?: TODO()
        val publicKey = injection.secrets.toPublicKey(body)
        logger.debug("public:key:transmitter: ${injection.secrets.hash(publicKey.encoded)}")
        val sessionId = UUID.randomUUID()
        val secretKey = injection.secrets.newSecretKey()
        logger.debug("secret:key: ${injection.secrets.hash(secretKey.encoded)}")
        injection.sessions.secureConnection = SecureConnection(
            sessionId = sessionId,
            expires = now + 1.minutes,
            secretKey = secretKey,
            publicKey = publicKey,
        )
        logger.debug("session:expires: ${Date(injection.sessions.secureConnection!!.expires.inWholeMilliseconds)}") // todo
        logger.debug("session:ID: $sessionId")
        val payload = sessionId.toString().toByteArray()
        logger.debug("payload: ${injection.secrets.hash(payload)}")
        val privateKey = injection.sessions.privateKey ?: TODO()
        val list = listOf(
            injection.secrets.base64(keys.publicKey.encoded),
            injection.secrets.base64(injection.secrets.encrypt(publicKey, secretKey.encoded)),
            injection.secrets.base64(injection.secrets.encrypt(secretKey, payload)),
            injection.secrets.base64(injection.secrets.sign(privateKey, payload)),
        )
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = emptyMap(),
            body = list.joinToString(separator = "\n").toByteArray(),
        )
    }

    private fun response(secureConnection: SecureConnection, payload: ByteArray): HttpResponse {
        val privateKey = injection.sessions.privateKey ?: TODO()
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = emptyMap(),
            body = listOf(
                injection.secrets.base64(injection.secrets.encrypt(secureConnection.secretKey, payload)),
                injection.secrets.base64(injection.secrets.sign(privateKey, secureConnection.sessionId.toString().toByteArray() + payload)),
            ).joinToString(separator = "\n").toByteArray(),
        )
    }

    private fun onPostDouble(request: HttpRequest): HttpResponse {
        logger.debug("on post double...")
        val secureConnection = injection.sessions.secureConnection
        val now = System.currentTimeMillis().milliseconds
        if (secureConnection == null) {
            return HttpResponse(
                version = "1.1",
                code = 500,
                message = "Internal Server Error",
                headers = mapOf("message" to "No session!"),
                body = "todo".toByteArray(),
            )
        }
        if (secureConnection.expires < now) {
            return HttpResponse(
                version = "1.1",
                code = 500,
                message = "Internal Server Error",
                headers = mapOf("message" to "Session expired!"),
                body = "todo".toByteArray(),
            )
        }
        val body = request.body ?: return HttpResponse(
            version = "1.1",
            code = 500,
            message = "Internal Server Error",
            headers = mapOf("message" to "No body!"),
            body = "todo".toByteArray(),
        )
        val secretKey = secureConnection.secretKey
        val split = String(body).split("\n")
        check(split.size == 2)
        val payload = injection.secrets.decrypt(secretKey, injection.secrets.base64(split[0]))
        val sig = injection.secrets.base64(split[1])
        injection.secrets.verify(secureConnection.publicKey, secureConnection.sessionId.toString().toByteArray() + payload, sig)
        val number = String(payload).toInt()
        check(number in 1..128)
        injection.sessions.secureConnection = null
        return response(
            secureConnection = secureConnection,
            payload = "${number * 2}".toByteArray(),
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
        val response = when (val route = mapping[request.query]) {
            null -> HttpResponse(
                version = "1.1",
                code = 404,
                message = "Not Found",
                headers = emptyMap(),
                body = null,
            )
            else -> when (val transform = route[request.method]) {
                null -> HttpResponse(
                    version = "1.1",
                    code = 405,
                    message = "Method Not Allowed",
                    headers = emptyMap(),
                    body = null,
                )
                else -> transform(request)
            }
        }
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
