package com.github.bjoernpetersen.deskbot.api

import com.github.bjoernpetersen.jmusicbot.InitializationException
import com.github.bjoernpetersen.jmusicbot.Loggable
import com.google.common.util.concurrent.ThreadFactoryBuilder
import java.io.Closeable
import java.io.IOException
import java.net.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class Broadcaster @Throws(InitializationException::class) constructor(private val port: Int, groupAddress: String, message: String) : Loggable, Closeable {
    private val cachedLogger: Logger = createLogger()
    private val groupAddress: InetAddress
    private val message: ByteArray
    private val scheduler: ScheduledExecutorService

    private val sockets: List<MulticastSocket>

    init {
        this.groupAddress = try {
            InetAddress.getByName(groupAddress)
        } catch (e: UnknownHostException) {
            throw InitializationException(e)
        }
        this.message = message.toByteArray(Charsets.UTF_8)
        this.scheduler = Executors.newSingleThreadScheduledExecutor(ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("UDP-broadcast-%d")
                .build()
        )

        val networkInterfaces = findNetworkInterfaces()
        this.sockets = networkInterfaces.map {
            try {
                val socket = MulticastSocket()
                socket.networkInterface = it
                socket.broadcast = true
                socket.joinGroup(this.groupAddress)
                logFine("Created socket for network interface ${it.name}")
                socket
            } catch (e: SocketException) {
                throw InitializationException(e)
            } catch (e: IOException) {
                throw InitializationException(e)
            }
        }

        start()
    }

    private fun findNetworkInterfaces(): List<NetworkInterface> {
        return NetworkInterface.getNetworkInterfaces().asSequence().filter {
            !it.isLoopback
                    && !it.isVirtual
                    && it.isUp
                    && it.supportsMulticast()
        }.toList()
    }

    override fun getLogger(): Logger = cachedLogger

    private fun start() {
        scheduler.scheduleWithFixedDelay({ sockets.forEach { broadcast(it) } }, 1, 2, TimeUnit.SECONDS)
    }

    private fun broadcast(socket: MulticastSocket) {
        val packet = createPacket()
        try {
            socket.send(packet)
        } catch (e: IOException) {
            logInfo(
                    e,
                    "Could not broadcast state packet (interface: %s)",
                    socket.networkInterface.name
            )
        }
    }

    private fun createPacket(): DatagramPacket {
        return DatagramPacket(message, message.size, groupAddress, port)
    }

    @Throws(IOException::class)
    override fun close() {
        scheduler.shutdown()
        try {
            scheduler.awaitTermination(500, TimeUnit.MILLISECONDS)
        } catch (e: InterruptedException) {
            throw IOException(e)
        } finally {
            sockets.forEach { it.close() }
        }
    }
}