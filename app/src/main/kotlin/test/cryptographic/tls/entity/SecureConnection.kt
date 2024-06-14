package test.cryptographic.tls.entity

import java.util.UUID
import javax.crypto.SecretKey
import kotlin.time.Duration

internal class SecureConnection(
    val sessionId: UUID,
    val expires: Duration,
    val secretKey: SecretKey,
)
