package test.cryptographic.tls.util

import java.net.Inet4Address
import java.net.NetworkInterface

internal object NetworkUtil {
    fun getLocalAddress(): Inet4Address {
        for (ni in NetworkInterface.getNetworkInterfaces()) {
            for (address in ni.inetAddresses) {
                if (address.isLoopbackAddress) continue
                if (address is Inet4Address) return address
            }
        }
        TODO()
    }
}
