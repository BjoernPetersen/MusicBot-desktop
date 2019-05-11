package net.bjoernpetersen.deskbot.impl

import com.google.common.util.concurrent.ThreadFactoryBuilder
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.ServerConstraints
import java.io.Closeable
import java.io.IOException
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.MulticastSocket
import java.net.NetworkInterface
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

private typealias Constraints = ServerConstraints.Broadcast

class Broadcaster @Throws(IOException::class) constructor() : Closeable {

    private val logger = KotlinLogging.logger {}

    private val groupAddress: InetAddress = InetAddress.getByName(Constraints.groupAdress)
    private val message: ByteArray = Constraints.message.toByteArray(Charsets.UTF_8)
    private val scheduler: ScheduledExecutorService = Executors
        .newSingleThreadScheduledExecutor(
            ThreadFactoryBuilder()
                .setDaemon(true)
                .setNameFormat("UDP-broadcast-%d")
                .build()
        )

    private val sockets: List<MulticastSocket> = findNetworkInterfaces().map {
        val socket = MulticastSocket()
        socket.networkInterface = it
        socket.broadcast = true
        socket.joinGroup(this.groupAddress)
        logger.debug { "Created socket for network interface ${it.name}" }
        socket
    }

    private fun findNetworkInterfaces(): List<NetworkInterface> {
        return NetworkInterface.getNetworkInterfaces().asSequence().filter {
            !it.isLoopback &&
                !it.isVirtual &&
                it.isUp &&
                it.supportsMulticast()
        }.toList()
    }

    fun start() {
        scheduler
            .scheduleWithFixedDelay(
                { sockets.forEach { broadcast(it) } },
                1,
                2,
                TimeUnit.SECONDS
            )
    }

    private fun broadcast(socket: MulticastSocket) {
        val packet = createPacket()
        try {
            socket.send(packet)
        } catch (e: IOException) {
            logger.info(e) {
                "Could not broadcast state packet (interface: ${socket.networkInterface.name})"
            }
        }
    }

    private fun createPacket(): DatagramPacket {
        return DatagramPacket(message, message.size, groupAddress, Constraints.port)
    }

    @Throws(IOException::class)
    override fun close() {
        scheduler.shutdown()
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                logger.warn { "Broadcaster did not shutdown in a second" }
                scheduler.shutdownNow()
            }
        } catch (e: InterruptedException) {
            throw IOException(e)
        } finally {
            sockets.forEach {
                try {
                    it.close()
                } catch (e: IOException) {
                    logger.error(e) { "Could not close socket" }
                }
            }
        }
    }
}
