package test.cryptographic.tls.provider

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import test.cryptographic.tls.entity.SessionStartResponse
import java.net.URL
import java.security.PrivateKey
import java.security.PublicKey
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey

internal class FinalRemotes(
    private val address: URL,
    private val serializer: Serializer,
    private val secrets: Secrets,
) : Remotes {
    private val client = OkHttpClient.Builder()
        .callTimeout(5, TimeUnit.SECONDS)
//        .connectTimeout(5, TimeUnit.SECONDS)
//        .writeTimeout(5, TimeUnit.SECONDS)
//        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    override fun sessionStart(
        publicKey: PublicKey,
        privateKey: PrivateKey,
    ): SessionStartResponse {
        return client.newCall(
            request = Request.Builder()
                .url(URL(address, "session/start"))
                .method("POST", publicKey.encoded.toRequestBody())
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> {
                    val body = response.body ?: error("No body!")
                    val bytes = body.bytes()
                    val text = String(bytes)
                    val split = text.split("\n")
                    check(split.size == 4)
                    val publicKeyReceiver = secrets.toPublicKey(secrets.base64(split[0]))
                    val secretKey = secrets.toSecretKey(secrets.decrypt(privateKey, secrets.base64(split[1])))
                    val payload = secrets.decrypt(secretKey, secrets.base64(split[2]))
                    println("[Remotes]: payload: ${secrets.hash(payload)}")
                    val sig = secrets.base64(split[3])
                    secrets.verify(publicKeyReceiver, message = payload, sig = sig)
                    SessionStartResponse(
                        publicKey = publicKeyReceiver,
                        secretKey = secretKey,
                        sessionId = UUID.fromString(String(payload)),
                    )
                }
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    override fun double(
        secretKey: SecretKey,
        privateKey: PrivateKey,
        publicKey: PublicKey,
        sessionId: UUID,
        number: Int,
    ): Int {
        val payload = "$number".toByteArray()
        val list = listOf(
            secrets.base64(secrets.encrypt(secretKey, payload)),
            secrets.base64(secrets.sign(privateKey, sessionId.toString().toByteArray() + payload)),
        )
        return client.newCall(
            request = Request.Builder()
                .url(URL(address, "double"))
                .method("POST", list.joinToString(separator = "\n").toByteArray().toRequestBody())
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> {
                    val body = response.body ?: error("No body!")
                    val split = String(body.bytes()).split("\n")
                    check(split.size == 2)
                    val bytes = secrets.decrypt(secretKey, secrets.base64(split[0]))
                    secrets.verify(
                        publicKey = publicKey,
                        message = sessionId.toString().toByteArray() + bytes,
                        sig = secrets.base64(split[1]),
                    )
                    String(bytes).toInt()
                }
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }
}
