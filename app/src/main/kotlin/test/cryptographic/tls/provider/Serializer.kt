package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.Keys

internal interface Serializer {
    val keys: Transformer<Keys, ByteArray>
    val ints: Transformer<Int, ByteArray>
}
