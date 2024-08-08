package test.cryptographic.tls.module.receiver

import sp.kx.bytes.readInt
import sp.kx.bytes.write
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
    )

    override var requested: Map<UUID, Duration>
        get() {
            return injection.locals.requested
        }
        set(value) {
            injection.locals.requested = value
        }

    private fun onPostDouble(request: HttpRequest): HttpResponse {
        logger.debug("on post double...")
        return map(
            request = request,
            transform = {
                val number = it.readInt()
                check(number in 1..1024)
                val bytes = ByteArray(4)
                bytes.write(value = number * 2)
                TLSResponse.OK(encoded = bytes)
            },
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
