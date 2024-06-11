package test.cryptographic.tls.provider

import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.concurrent.TimeUnit

internal class FinalRemotes(
    private val address: URL,
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

    companion object {
        private val client = OkHttpClient.Builder()
            .callTimeout(5, TimeUnit.SECONDS)
            .connectTimeout(5, TimeUnit.SECONDS)
            .writeTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
    }
}
