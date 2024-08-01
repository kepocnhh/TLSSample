package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.Keys
import java.net.URL
import java.util.UUID
import kotlin.time.Duration

internal interface Locals {
    var address: URL?
    var keys: Keys?
    var requested: Map<UUID, Duration>
}
