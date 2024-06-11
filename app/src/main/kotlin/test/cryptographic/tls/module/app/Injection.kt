package test.cryptographic.tls.module.app

import test.cryptographic.tls.provider.Contexts
import test.cryptographic.tls.provider.Loggers

internal class Injection(
    val contexts: Contexts,
    val loggers: Loggers,
)
