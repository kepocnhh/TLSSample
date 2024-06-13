package test.cryptographic.tls.entity

import java.security.PublicKey

internal class StartSessionRequest(
    val publicKey: PublicKey,
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is StartSessionRequest -> other.publicKey.encoded.contentEquals(publicKey.encoded)
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
