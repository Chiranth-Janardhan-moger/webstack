package com.example.server

import java.net.Inet4Address
import java.net.NetworkInterface

object WifiUtils {

    /**
     * Resolves the primary local IPv4 address of the device on the active local network interface.
     * Prefers wireless interfaces (wlan, wifi, ap) over generic interfaces.
     */
    fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList() ?: return null

            // Pass 1: Prioritize Wi-Fi and hotspot interfaces
            for (iface in interfaces) {
                if (!iface.isUp || iface.isLoopback) continue
                val name = iface.name.lowercase()
                if (name.contains("wlan") || name.contains("wifi") || name.contains("ap")) {
                    val addr = findSiteLocalIpv4(iface)
                    if (addr != null) return addr
                }
            }

            // Pass 2: Fallback to any active non-loopback site-local IPv4 interface
            for (iface in interfaces) {
                if (!iface.isUp || iface.isLoopback) continue
                val addr = findSiteLocalIpv4(iface)
                if (addr != null) return addr
            }

            // Pass 3: Any non-loopback IPv4 address
            for (iface in interfaces) {
                if (!iface.isUp || iface.isLoopback) continue
                for (address in iface.inetAddresses) {
                    if (!address.isLoopbackAddress && address is Inet4Address) {
                        val host = address.hostAddress
                        if (!host.isNullOrBlank()) return host
                    }
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    private fun findSiteLocalIpv4(iface: NetworkInterface): String? {
        for (address in iface.inetAddresses) {
            if (!address.isLoopbackAddress && address is Inet4Address) {
                val host = address.hostAddress ?: continue
                if (host.startsWith("192.168.") || host.startsWith("10.") || isClassBPrivate(host)) {
                    return host
                }
            }
        }
        return null
    }

    private fun isClassBPrivate(ip: String): Boolean {
        if (!ip.startsWith("172.")) return false
        val parts = ip.split(".")
        if (parts.size >= 2) {
            val secondOctet = parts[1].toIntOrNull() ?: return false
            return secondOctet in 16..31
        }
        return false
    }
}
