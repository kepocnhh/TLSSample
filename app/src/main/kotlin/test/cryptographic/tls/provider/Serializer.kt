package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.SecureConnection
import test.cryptographic.tls.entity.StartSessionRequest
import test.cryptographic.tls.entity.StartSessionResponse

internal interface Serializer {
    val secureConnection: Transformer<SecureConnection, ByteArray>
    val startSessionRequest: Transformer<StartSessionRequest, ByteArray>
    val startSessionResponse: Transformer<StartSessionResponse, ByteArray>
}
