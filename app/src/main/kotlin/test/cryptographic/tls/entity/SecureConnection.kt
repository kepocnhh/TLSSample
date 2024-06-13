package test.cryptographic.tls.entity

import java.security.PublicKey
import java.util.UUID
import kotlin.time.Duration

internal class SecureConnection(
    val sessionId: UUID,
    val expires: Duration,
    val publicKey: PublicKey,
)
