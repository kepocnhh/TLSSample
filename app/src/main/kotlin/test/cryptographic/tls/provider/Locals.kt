package test.cryptographic.tls.provider

import test.cryptographic.tls.entity.Keys
import java.net.URL

internal interface Locals {
    var address: URL?
    var keys: Keys?
}
