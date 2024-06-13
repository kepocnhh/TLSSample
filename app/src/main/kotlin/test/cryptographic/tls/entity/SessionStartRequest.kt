package test.cryptographic.tls.entity

import java.security.PublicKey

internal class SessionStartRequest(
    val publicKey: PublicKey,
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is SessionStartRequest -> other.publicKey.encoded.contentEquals(publicKey.encoded)
            else -> false
        }
    }

    override fun hashCode(): Int {
        return publicKey.encoded.contentHashCode()
    }

    override fun toString(): String {
        return "StartSessionRequest(publicKey: ${publicKey.encoded.size})"
    }
}
