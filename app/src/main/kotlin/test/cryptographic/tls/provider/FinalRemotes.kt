package test.cryptographic.tls.provider

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import sp.kx.http.TLSEnvironment
import sp.kx.http.TLSTransmitter
import java.net.URL
import java.util.concurrent.TimeUnit

internal class FinalRemotes(
    private val address: URL,
    private val tls: TLSEnvironment,
) : Remotes {
    private val client = OkHttpClient.Builder()
        .callTimeout(5, TimeUnit.SECONDS)
        .build()

    private fun <T : Any> execute(
        method: String,
        query: String,
        encoded: ByteArray,
        decode: (ByteArray) -> T,
    ): T {
        // todo key pair
        val methodCode: Byte = TLSEnvironment.getMethodCode(method = method)
        val encodedQuery = query.toByteArray()
        val tlsTransmitter = TLSTransmitter.build(
            env = tls,
            methodCode = methodCode,
            encodedQuery = encodedQuery,
            encoded = encoded,
        )
        return client.newCall(
            request = Request.Builder()
                .url(URL(address, query))
                .method(method, tlsTransmitter.body.toRequestBody())
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> {
                    val body = response.body?.bytes() ?: error("No body!")
                    val responseEncoded = TLSTransmitter.fromResponse(
                        env = tls,
                        methodCode = methodCode,
                        encodedQuery = encodedQuery,
                        secretKey = tlsTransmitter.secretKey,
                        requestID = tlsTransmitter.id,
                        responseCode = response.code,
                        message = response.message,
                        body = body,
                    )
                    decode(responseEncoded)
                }
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    override fun double(number: Int): Int {
        val bytes = ByteArray(4)
        bytes.set(0, number.shr(24).toByte())
        bytes.set(0 + 1, number.shr(16).toByte())
        bytes.set(0 + 2, number.shr(8).toByte())
        bytes.set(0 + 3, number.toByte())
        // todo key pair
        return execute(
            method = "POST",
            query = "/double",
            encoded = bytes,
            decode = {
                it.get(0).toInt().and(0xff).shl(24)
                    .or(it.get(0 + 1).toInt().and(0xff).shl(16))
                    .or(it.get(0 + 2).toInt().and(0xff).shl(8))
                    .or(it.get(0 + 3).toInt().and(0xff))
            },
        )
    }
}
