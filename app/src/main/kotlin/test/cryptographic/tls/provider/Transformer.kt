package test.cryptographic.tls.provider

internal interface Transformer<D : Any, E : Any> {
    fun encode(decoded: D): E
    fun decode(encoded: E): D
}
