package test.cryptographic.tls.util

import java.net.Inet4Address
import java.net.NetworkInterface

internal object NetworkUtil {
    fun getLocalAddress(): Inet4Address {
        for (ni in NetworkInterface.getNetworkInterfaces()) {
            // todo network interface is up
            for (address in ni.inetAddresses) {
                if (address.isLoopbackAddress) continue
                if (address is Inet4Address) return address
            }
        }
        TODO()
    }
}
