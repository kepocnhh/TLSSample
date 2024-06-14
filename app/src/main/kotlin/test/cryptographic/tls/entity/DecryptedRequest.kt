package test.cryptographic.tls.entity

internal class DecryptedRequest<T : Any>(
    val session: Session,
    val payload: T,
)
