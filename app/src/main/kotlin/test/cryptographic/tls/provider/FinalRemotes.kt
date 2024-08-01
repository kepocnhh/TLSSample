package test.cryptographic.tls.provider

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import test.cryptographic.tls.entity.SessionStartResponse
import test.cryptographic.tls.util.BytesUtil
import test.cryptographic.tls.util.toHEX
import java.net.URL
import java.security.PrivateKey
import java.security.PublicKey
import java.util.Date
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.crypto.SecretKey
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

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

    private fun onDouble(
        secretKey: SecretKey,
        publicKey: PublicKey,
        responseBody: ByteArray,
        encodedSpec: ByteArray,
        methodCode: Byte,
        requestID: UUID,
    ): Int {
        val tag = "[Remotes:onDouble]"
        val encryptedPayload = ByteArray(BytesUtil.readInt(responseBody, index = 0))
        System.arraycopy(responseBody, 4, encryptedPayload, 0, encryptedPayload.size)
        println("$tag: response encrypted payload: ${encryptedPayload.toHEX()}") // todo
        val payload = secrets.decrypt(secretKey, encryptedPayload)
        println("$tag: response payload: ${payload.toHEX()}") // todo
        val responseEncoded = ByteArray(BytesUtil.readInt(payload, index = 0))
        System.arraycopy(payload, 4, responseEncoded, 0, responseEncoded.size)
        println("$tag: response encoded: ${responseEncoded.toHEX()}") // todo
        val responseTime = BytesUtil.readLong(payload, index = 4 + responseEncoded.size).milliseconds
        println("$tag: response time: ${Date(responseTime.inWholeMilliseconds)}") // todo
        val signature = ByteArray(BytesUtil.readInt(responseBody, index = 4 + encryptedPayload.size))
        System.arraycopy(responseBody, 4 + encryptedPayload.size + 4, signature, 0, signature.size)
        println("$tag: response signature: ${signature.toHEX()}") // todo
        println("$tag: response signature hash: ${secrets.hash(signature)}") // todo
        val signatureData = ByteArray(payload.size + 16 + 1 + encodedSpec.size)
        System.arraycopy(payload, 0, signatureData, 0, payload.size)
        BytesUtil.writeBytes(signatureData, index = payload.size, requestID)
        signatureData[payload.size + 16] = methodCode
        System.arraycopy(encodedSpec, 0, signatureData, payload.size + 16 + 1, encodedSpec.size)
        println("$tag: response signature data: ${signatureData.toHEX()}") // todo
        println("$tag: response signature data hash: ${secrets.hash(signatureData)}") // todo
        val verified = secrets.verify(publicKey, signatureData, signature)
        if (!verified) TODO("FinalRemotes:onDouble:!verified")
        val now = System.currentTimeMillis().milliseconds
        // todo now < requestTime
        val maxTime = 1.minutes
        if (now - responseTime > maxTime) {
            TODO("FinalRemotes:onDouble:time")
        }
        val responseDecoded = BytesUtil.readInt(responseEncoded, index = 0)
        println("$tag: response decoded: $responseDecoded") // todo
        return responseDecoded
    }

    override fun double(
        publicKey: PublicKey,
        privateKey: PrivateKey,
        number: Int,
    ): Int {
        val tag = "[Remotes]"
        val requestTime = System.currentTimeMillis().milliseconds
        println("$tag: request time: ${Date(requestTime.inWholeMilliseconds)}") // todo
        val requestID = UUID.randomUUID()
        println("$tag: request ID: $requestID") // todo
        val encoded = ByteArray(4)
        BytesUtil.writeBytes(encoded, index = 0, number)
        val payload = ByteArray(4 + encoded.size + 8 + 16)
        BytesUtil.writeBytes(payload, index = 0, encoded.size)
        System.arraycopy(encoded, 0, payload, 4, encoded.size)
        BytesUtil.writeBytes(payload, index = 4 + encoded.size, requestTime.inWholeMilliseconds)
        BytesUtil.writeBytes(payload, index = 4 + encoded.size + 8, requestID)
        println("$tag: payload: ${payload.toHEX()}") // todo
        val spec = "/double"
        println("$tag: spec: \"$spec\"") // todo
        val method = "POST"
        val methodCode: Byte = 1 // POST
        println("$tag: method: \"$method\"") // todo
        val secretKey = secrets.newSecretKey()
        println("$tag: secret key: ${secretKey.encoded.toHEX()}") // todo
        val encryptedSK = secrets.encrypt(publicKey, secretKey.encoded)
        println("$tag: encrypted secret key: ${encryptedSK.toHEX()}") // todo
        val encryptedPayload = secrets.encrypt(secretKey, payload)
        println("$tag: encrypted payload: ${encryptedPayload.toHEX()}") // todo
        val encodedSpec = spec.toByteArray()
        val signatureData = ByteArray(payload.size + 1 + encodedSpec.size + secretKey.encoded.size)
        System.arraycopy(payload, 0, signatureData, 0, payload.size)
        signatureData[payload.size] = methodCode
        System.arraycopy(encodedSpec, 0, signatureData, payload.size + 1, encodedSpec.size)
        System.arraycopy(secretKey.encoded, 0, signatureData, payload.size + 1 + encodedSpec.size, secretKey.encoded.size)
        println("$tag: signature data: ${signatureData.toHEX()}") // todo
        println("$tag: signature data hash: ${secrets.hash(signatureData)}") // todo
        val signature = secrets.sign(privateKey, signatureData)
        println("$tag: signature: ${signature.toHEX()}") // todo
        val requestBody = ByteArray(4 + encryptedSK.size + 4 + encryptedPayload.size + 4 + signature.size)
        BytesUtil.writeBytes(requestBody, index = 0, encryptedSK.size)
        System.arraycopy(encryptedSK, 0, requestBody, 4, encryptedSK.size)
        BytesUtil.writeBytes(requestBody, index = 4 + encryptedSK.size, encryptedPayload.size)
        System.arraycopy(encryptedPayload, 0, requestBody, 4 + encryptedSK.size + 4, encryptedPayload.size)
        BytesUtil.writeBytes(requestBody, index = 4 + encryptedSK.size + 4 + encryptedPayload.size, signature.size)
        System.arraycopy(signature, 0, requestBody, 4 + encryptedSK.size + 4 + encryptedPayload.size + 4, signature.size)
        return client.newCall(
            request = Request.Builder()
                .url(URL(address, spec))
                .method(method, requestBody.toRequestBody())
                .build(),
        ).execute().use { response ->
            when (response.code) {
                200 -> {
                    val responseBody = response.body?.bytes() ?: error("No body!")
                    println("$tag: response body: ${responseBody.toHEX()}") // todo
                    println("$tag: response body hash: ${secrets.hash(responseBody)}") // todo
                    onDouble(
                        secretKey = secretKey,
                        publicKey = publicKey,
                        responseBody = responseBody,
                        encodedSpec = encodedSpec,
                        methodCode = methodCode,
                        requestID = requestID,
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
