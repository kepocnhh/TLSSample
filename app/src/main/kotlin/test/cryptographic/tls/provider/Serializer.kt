package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.DecryptedRequest
import test.cryptographic.tls.entity.Session
import test.cryptographic.tls.entity.Keys
import test.cryptographic.tls.entity.SecureConnection
import test.cryptographic.tls.entity.SessionStartRequest
import test.cryptographic.tls.entity.SessionStartResponse

internal interface Serializer {
    val secureConnection: Transformer<SecureConnection, ByteArray>
    val session: Transformer<Session, ByteArray>
    val sessionStartRequest: Transformer<SessionStartRequest, ByteArray>
    val sessionStartResponse: Transformer<SessionStartResponse, ByteArray>
    val keys: Transformer<Keys, ByteArray>
    val ints: Transformer<Int, ByteArray>
    fun <T : Any> decryptedRequest(transformer: Transformer<T, ByteArray>): Transformer<DecryptedRequest<T>, ByteArray>
}
