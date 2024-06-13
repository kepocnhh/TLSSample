package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.SecureConnection
import java.net.URL
import java.security.PublicKey

internal interface Locals {
    var address: URL?
    var publicKey: PublicKey?
    var secureConnection: SecureConnection?
}
