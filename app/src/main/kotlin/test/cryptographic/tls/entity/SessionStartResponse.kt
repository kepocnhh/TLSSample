package test.cryptographic.tls.entity

import java.util.Objects
import javax.crypto.SecretKey

internal class SessionStartResponse(
    val secretKey: SecretKey,
    val session: Session,
) {
    override fun equals(other: Any?): Boolean {
        return when (other) {
            is SessionStartResponse -> {
                other.secretKey.encoded.contentEquals(secretKey.encoded) && other.session == session
            }
            else -> false
        }
    }

    override fun hashCode(): Int {
        return Objects.hash(
            secretKey.encoded.contentHashCode(),
            session,
        )
    }

    override fun toString(): String {
        return "StartSessionResponse(secretKey: ${secretKey.encoded.size}, session: $session)"
    }
}
