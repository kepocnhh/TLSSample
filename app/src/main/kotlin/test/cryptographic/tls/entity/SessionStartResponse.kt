package test.cryptographic.tls.entity

import java.security.PublicKey
import java.util.Objects
import java.util.UUID

internal class SessionStartResponse(
    val publicKey: PublicKey,
    val sessionId: UUID,
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is SessionStartResponse -> {
                other.publicKey.encoded.contentEquals(publicKey.encoded) && other.sessionId == sessionId
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(
            publicKey.encoded.contentHashCode(),
            sessionId,
        )
    }

    override fun toString(): String {
        return "StartSessionResponse(publicKey: ${publicKey.encoded.size}, sessionId: $sessionId)"
    }
}
