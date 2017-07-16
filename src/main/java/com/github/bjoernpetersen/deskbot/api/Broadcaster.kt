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
    private val socket: DatagramSocket

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
        this.socket = try {
            DatagramSocket()
        } catch (e: SocketException) {
            throw InitializationException(e)
        }
        socket.broadcast = true

        start()
    }

    override fun getLogger(): Logger = cachedLogger

    private fun start() {
        scheduler.scheduleWithFixedDelay({ broadcast() }, 1, 2, TimeUnit.SECONDS)
    }

    private fun broadcast() {
        val packet = createPacket()
        try {
            socket.send(packet)
        } catch (e: IOException) {
            logInfo("Could not broadcast state packet", e)
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
        }

        socket.close()
    }
}