package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.SecureConnection
import java.security.PrivateKey

internal class Sessions {
    var privateKey: PrivateKey? = null
    var secureConnection: SecureConnection? = null
}
