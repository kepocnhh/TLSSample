package test.cryptographic.tls.provider

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import test.cryptographic.tls.entity.DecryptedRequest
import test.cryptographic.tls.entity.Session
import test.cryptographic.tls.entity.SessionStartRequest
import test.cryptographic.tls.entity.SessionStartResponse
import java.net.URL
import java.security.PrivateKey
import java.security.PublicKey
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

    override fun version(): String {
        client.newCall(
            request = Request.Builder()
                .url(URL(address, "version"))
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> return String(response.body!!.bytes())
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    override fun sessionStart(privateKey: PrivateKey, request: SessionStartRequest): SessionStartResponse {
        println("session start...") // todo
        client.newCall(
            request = Request.Builder()
                .url(URL(address, "session/start"))
                .method("POST", serializer.sessionStartRequest.encode(request).toRequestBody())
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> {
                    val secretKey = secrets.toSecretKey(
                        secrets.decrypt(
                            privateKey = privateKey,
                            encrypted = secrets.base64(
                                response.header("secretKey", null) ?: TODO(),
                            ),
                        ),
                    )
                    println("secret:key: ${secrets.hash(secretKey.encoded)}") // todo
                    val session = serializer.session.decode(
                        secrets.decrypt(
                            secretKey = secretKey,
                            encrypted = secrets.base64(
                                response.header("session", null) ?: TODO(),
                            ),
                        ),
                    )
                    return SessionStartResponse(
                        session = session,
                        secretKey = secretKey,
                    )
                }
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    private inner class FinalEncryptedRemotes(
        private val secretKey: SecretKey,
        private val session: Session,
    ) : EncryptedRemotes {
        private fun <T : Any> T.toRequestBody(
            transformer: Transformer<T, ByteArray>,
        ): RequestBody {
            val decryptedRequest = DecryptedRequest(
                session = session,
                payload = this,
            )
            println("session:ID: ${decryptedRequest.session.id}") // todo
            val encrypted = secrets.encrypt(
                secretKey = secretKey,
                decrypted = serializer.decryptedRequest(transformer).encode(decryptedRequest),
            )
            println("encrypted(${encrypted.size}): ${secrets.hash(encrypted)}") // todo
            return encrypted.toRequestBody()
        }

        private fun <T : Any> Response.fromResponseBody(transformer: Transformer<T, ByteArray>): T {
            val body = body ?: error("No body!")
            val decrypted = secrets.decrypt(
                secretKey = secretKey,
                encrypted = body.bytes(),
            )
            println("decrypted(${decrypted.size}): ${secrets.hash(decrypted)}") // todo
            return transformer.decode(decrypted)
        }

        override fun double(number: Int): Int {
            println("request -> double($number)...") // todo
            client.newCall(
                request = Request.Builder()
                    .url(URL(address, "double"))
                    .method("POST", number.toRequestBody(serializer.ints))
                    .build(),
            ).execute().use { response ->
                println("response <- double($number)...") // todo
                when (response.code) {
                    200 -> return response.fromResponseBody(serializer.ints)
                    else -> error("Unknown code: ${response.code}!")
                }
            }
        }
    }

    override fun encrypted(
        secretKey: SecretKey,
        session: Session,
    ): EncryptedRemotes {
        return FinalEncryptedRemotes(
            secretKey = secretKey,
            session = session,
        )
    }

    override fun sessionStart(publicKey: PublicKey): ByteArray {
        return client.newCall(
            request = Request.Builder()
                .url(URL(address, "session/start"))
                .method("POST", publicKey.encoded.toRequestBody())
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> response.body?.bytes() ?: error("No body!")
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }

    override fun double(encrypted: ByteArray): ByteArray {
        return client.newCall(
            request = Request.Builder()
                .url(URL(address, "double"))
                .method("POST", encrypted.toRequestBody())
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> response.body?.bytes() ?: error("No body!")
                else -> error("Unknown code: ${response.code}!")
            }
        }
    }
}
