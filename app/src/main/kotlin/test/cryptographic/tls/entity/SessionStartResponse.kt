package test.cryptographic.tls.entity

import java.security.PublicKey
import java.util.UUID
import javax.crypto.SecretKey

internal class SessionStartResponse(
    val secretKey: SecretKey,
    val sessionId: UUID,
    val publicKey: PublicKey,
)
