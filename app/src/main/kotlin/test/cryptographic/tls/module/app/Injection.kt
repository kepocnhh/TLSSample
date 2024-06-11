package test.cryptographic.tls.module.app

import test.cryptographic.tls.provider.Contexts
import test.cryptographic.tls.provider.Locals
import test.cryptographic.tls.provider.Loggers
import test.cryptographic.tls.provider.Remotes
import java.net.URL

internal class Injection(
    val contexts: Contexts,
    val loggers: Loggers,
    val locals: Locals,
    val remotes: (URL) -> Remotes,
)
