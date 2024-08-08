package test.cryptographic.tls.module.receiver

import sp.kx.http.HttpRequest
import sp.kx.http.HttpResponse
import sp.kx.http.HttpRouting
import sp.kx.http.TLSResponse
import sp.kx.http.TLSRouting
import test.cryptographic.tls.BuildConfig
import test.cryptographic.tls.entity.SecureConnection
import test.cryptographic.tls.module.app.Injection
import test.cryptographic.tls.util.BytesUtil
import test.cryptographic.tls.util.toHEX
import java.security.KeyPair
import java.security.PrivateKey
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

internal class ReceiverRouting(
    private val injection: Injection,
) : TLSRouting(env = injection.tls) {
    private val logger = injection.loggers.create("[Receiver|Routing]")

    private val mapping: Map<String, Map<String, (HttpRequest) -> HttpResponse>> = mapOf(
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

    override val keyPair: KeyPair get() {
        val keys = injection.locals.keys ?: error("No keys!")
        return KeyPair(
            keys.publicKey,
            injection.sessions.privateKey ?: error("No private key!"),
        )
    }

    override var requested: Map<UUID, Duration>
        get() {
            return injection.locals.requested
        }
        set(value) {
            injection.locals.requested = value
        }

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

    private fun onPostDouble(request: HttpRequest): HttpResponse {
        logger.debug("on post double...")
        return map(
            request = request,
            transform = TLSResponse::OK,
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
