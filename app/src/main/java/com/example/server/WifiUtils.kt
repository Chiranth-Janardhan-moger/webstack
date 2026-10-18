package com.example.server

import java.net.Inet4Address
import java.net.NetworkInterface

object WifiUtils {

    private val CELLULAR_INTERFACE_PREFIXES = listOf(
        "rmnet", "ccmni", "pdp", "wwan", "cellular", "mobile", "dun", "radio", "dummy"
    )

    private val WIRELESS_INTERFACE_PREFIXES = listOf(
        "wlan", "wifi", "ap", "softap", "swlan", "rndis"
    )

    /**
     * Resolves the primary local IPv4 address of the device on the active local network interface.
     * Strictly filters out cellular/carrier interfaces (rmnet, ccmni, etc.) and prioritizes
     * true local Wi-Fi and hotspot interfaces.
     */
    fun getLocalIpAddress(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList() ?: return null

            // Filter out down, loopback, and cellular interfaces
            val candidateInterfaces = interfaces.filter { iface ->
                iface.isUp && !iface.isLoopback && !isCellularInterface(iface.name)
            }

            // Pass 1: True Wi-Fi / Hotspot interfaces with 192.168.x.x (standard LAN)
            for (iface in candidateInterfaces) {
                if (isWirelessInterface(iface.name)) {
                    val addr = findClassCIpv4(iface)
                    if (addr != null) return addr
                }
            }

            // Pass 2: True Wi-Fi / Hotspot interfaces with any site-local IPv4 (10.x, 172.16-31.x)
            for (iface in candidateInterfaces) {
                if (isWirelessInterface(iface.name)) {
                    val addr = findSiteLocalIpv4(iface)
                    if (addr != null) return addr
                }
            }

            // Pass 3: Any other non-cellular interface with 192.168.x.x
            for (iface in candidateInterfaces) {
                val addr = findClassCIpv4(iface)
                if (addr != null) return addr
            }

            // Pass 4: Any candidate interface with private IPv4
            for (iface in candidateInterfaces) {
                val addr = findSiteLocalIpv4(iface)
                if (addr != null) return addr
            }

            // Pass 5: Fallback to any non-loopback candidate IPv4 address
            for (iface in candidateInterfaces) {
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

    private fun isCellularInterface(name: String): Boolean {
        val lower = name.lowercase()
        return CELLULAR_INTERFACE_PREFIXES.any { lower.contains(it) }
    }

    private fun isWirelessInterface(name: String): Boolean {
        val lower = name.lowercase()
        return WIRELESS_INTERFACE_PREFIXES.any { lower.contains(it) }
    }

    private fun findClassCIpv4(iface: NetworkInterface): String? {
        for (address in iface.inetAddresses) {
            if (!address.isLoopbackAddress && address is Inet4Address) {
                val host = address.hostAddress ?: continue
                if (host.startsWith("192.168.")) {
                    return host
                }
            }
        }
        return null
    }

    private fun findSiteLocalIpv4(iface: NetworkInterface): String? {
        for (address in iface.inetAddresses) {
            if (!address.isLoopbackAddress && address is Inet4Address) {
                val host = address.hostAddress ?: continue
                if (host.startsWith("192.168.") || isClassBPrivate(host) || host.startsWith("10.")) {
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
