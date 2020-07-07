package com.flangenet.stockcheck.Utilities

import java.net.InetAddress

class Pinger {

    object PingExample {
        @JvmStatic
        fun main(args: Array<String>) {
            try {
                val address: InetAddress = InetAddress.getByName("192.168.1.151")
                val reachable: Boolean = address.isReachable(1000)
                println("Is host reachable? $reachable")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }



}