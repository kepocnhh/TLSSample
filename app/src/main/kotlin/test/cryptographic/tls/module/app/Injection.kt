package test.cryptographic.tls.module.app

import test.cryptographic.tls.provider.Assets
import test.cryptographic.tls.provider.Contexts
import test.cryptographic.tls.provider.Locals
import test.cryptographic.tls.provider.Loggers
import test.cryptographic.tls.provider.Remotes
import test.cryptographic.tls.provider.Secrets
import test.cryptographic.tls.provider.Serializer
import test.cryptographic.tls.provider.Sessions
import java.net.URL

internal class Injection(
    val contexts: Contexts,
    val loggers: Loggers,
    val locals: Locals,
    val remotes: (URL) -> Remotes,
    val serializer: Serializer,
    val sessions: Sessions, // todo
    val assets: Assets,
    val secrets: Secrets,
)
