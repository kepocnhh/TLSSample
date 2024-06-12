package test.cryptographic.tls.module.receiver

import sp.kx.http.HttpRequest
import sp.kx.http.HttpResponse
import sp.kx.http.HttpRouting
import test.cryptographic.tls.BuildConfig
import test.cryptographic.tls.module.app.Injection
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class ReceiverRouting(
    private val injection: Injection,
) : HttpRouting {
    private val logger = injection.loggers.create("[Receiver|Routing]")

    private val mapping = mapOf(
        "/version" to mapOf(
            "GET" to ::onGetVersion,
        ),
        "/hello" to mapOf(
            "GET" to ::onGetHello,
        ),
        "/delay" to mapOf(
            "POST" to ::onPostDelay,
        ),
        "/double" to mapOf(
            "POST" to ::onPostDouble,
        ),
    )

    private fun onPostDouble(request: HttpRequest): HttpResponse {
        val body = request.body ?: error("No body!")
        val numberText = String(body)
        val number = numberText.toIntOrNull() ?: error("Wrong number!")
        if (number !in 1..128) TODO()
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = emptyMap(),
            body = (number * 2).toString().toByteArray(),
        )
    }

    private fun onPostDelay(request: HttpRequest): HttpResponse {
        val body = request.body ?: error("No body!")
        val durationText = String(body)
        val duration = durationText.toLongOrNull()?.milliseconds ?: error("Wrong duration!")
        val supported = setOf(3.seconds)
        if (!supported.contains(duration)) error("Duration $duration is not supported!")
        Thread.sleep(duration.inWholeMilliseconds)
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = emptyMap(),
            body = null,
        )
    }

    private fun onGetHello(request: HttpRequest): HttpResponse {
        return HttpResponse(
            version = "1.1",
            code = 200,
            message = "OK",
            headers = emptyMap(),
            body = null,
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
