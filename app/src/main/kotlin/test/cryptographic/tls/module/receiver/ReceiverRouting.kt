package test.cryptographic.tls.module.receiver

import sp.kx.http.HttpRequest
import sp.kx.http.HttpResponse
import sp.kx.http.HttpRouting
import test.cryptographic.tls.BuildConfig
import test.cryptographic.tls.module.app.Injection

internal class ReceiverRouting(
    private val injection: Injection,
) : HttpRouting {
    private val logger = injection.loggers.create("[Receiver|Routing]")

    private val mapping = mapOf(
        "/version" to mapOf(
            "GET" to ::onGetVersion,
        ),
    )

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
