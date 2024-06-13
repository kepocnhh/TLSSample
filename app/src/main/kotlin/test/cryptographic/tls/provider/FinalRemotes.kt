package test.cryptographic.tls.provider

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import test.cryptographic.tls.entity.SessionStartRequest
import java.net.URL
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

internal class FinalRemotes(
    private val address: URL,
    private val serializer: Serializer,
) : Remotes {
    override fun hello() {
        val request = Request.Builder()
            .url(URL(address, "hello"))
            .build()
        client.newCall(request).execute().use { response ->
            when (response.code) {
                200 -> {
                    // noop
                }
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    override fun delay(duration: Duration) {
        val body = duration.inWholeMilliseconds.toString().toRequestBody()
        val request = Request.Builder()
            .url(URL(address, "delay"))
            .method("POST", body)
            .build()
        client.newCall(request).execute().use { response ->
            when (response.code) {
                200 -> {
                    // noop
                }
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    override fun double(number: Int): Int {
        val request = Request.Builder()
            .url(URL(address, "double"))
            .method("POST", number.toString().toRequestBody())
            .build()
        client.newCall(request).execute().use { response ->
            when (response.code) {
                200 -> {
                    val body = response.body ?: error("No body!")
                    return body.string().toInt()
                }
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    override fun sessionStart(request: SessionStartRequest): ByteArray {
        client.newCall(
            request = Request.Builder()
                .url(URL(address, "session/start"))
                .method("POST", serializer.sessionStartRequest.encode(request).toRequestBody())
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> {
                    val body = response.body ?: error("No body!")
                    return body.bytes()
                }
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    companion object {
        private val client = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }
}
