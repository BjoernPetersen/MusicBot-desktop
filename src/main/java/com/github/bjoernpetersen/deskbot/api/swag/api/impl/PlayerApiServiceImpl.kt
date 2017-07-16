package com.github.bjoernpetersen.deskbot.api.swag.api.impl

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException
import com.github.bjoernpetersen.deskbot.api.swag.api.PlayerApiService
import com.github.bjoernpetersen.jmusicbot.MusicBot
import com.github.bjoernpetersen.jmusicbot.ProviderManager
import com.github.bjoernpetersen.jmusicbot.playback.Player
import com.github.bjoernpetersen.jmusicbot.playback.QueueEntry
import com.github.bjoernpetersen.jmusicbot.user.*
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext


class PlayerApiServiceImpl : PlayerApiService() {
    private lateinit var providerManager: ProviderManager
    private lateinit var player: Player
    private lateinit var userManager: UserManager

    override fun initialize(bot: MusicBot) {
        providerManager = bot.providerManager
        player = bot.player
        userManager = bot.userManager
    }

    @Throws(NotFoundException::class)
    override fun dequeue(authorization: String,
                         queueEntry: ModelQueueEntry,
                         securityContext: SecurityContext): Response {
        val user: User
        try {
            user = userManager.fromToken(authorization)
        } catch (e: InvalidTokenException) {
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }

        if (!user.permissions.contains(Permission.SKIP)) {
            return Response.status(Response.Status.FORBIDDEN).build()
        }

        val paramSong = queueEntry.song
        val song = lookupSong(providerManager, paramSong.id, paramSong.providerId)
        if (song != null) {
            val queueUser: User
            try {
                queueUser = userManager.getUser(queueEntry.userName)
            } catch (e: UserNotFoundException) {
                return Response.status(Response.Status.UNAUTHORIZED).build()
            }

            val entry = QueueEntry(song, queueUser)
            player.queue.remove(entry)
            return getQueue(securityContext)
        } else {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @Throws(NotFoundException::class)
    override fun enqueue(authorization: String, songId: String,
                         providerId: String,
                         securityContext: SecurityContext): Response {
        val user: User
        try {
            user = userManager.fromToken(authorization)
        } catch (e: InvalidTokenException) {
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }

        val song = lookupSong(providerManager, songId, providerId)
        if (song != null) {
            val entry = QueueEntry(song, user)
            player.queue.append(entry)
            return getQueue(securityContext)
        } else {
            return Response.status(Response.Status.NOT_FOUND).build()
        }
    }

    @Throws(NotFoundException::class)
    override fun getPlayerState(securityContext: SecurityContext): Response =
            Response.ok(player.state.convert()).build()

    @Throws(NotFoundException::class)
    override fun getQueue(securityContext: SecurityContext): Response =
            Response.ok(player.queue.toList().convert()).build()

    @Throws(NotFoundException::class)
    override fun nextSong(authorization: String, securityContext: SecurityContext): Response {
        val user: User
        try {
            user = userManager.fromToken(authorization)
        } catch (e: InvalidTokenException) {
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }

        if (!user.permissions.contains(Permission.SKIP)) {
            return Response.status(Response.Status.FORBIDDEN).build()
        }

        try {
            player.next()
        } catch (e: InterruptedException) {
            return Response.serverError().entity("Interrupted").build()
        }

        return getPlayerState(securityContext)
    }

    @Throws(NotFoundException::class)
    override fun pausePlayer(securityContext: SecurityContext): Response {
        player.pause()
        return getPlayerState(securityContext)
    }

    @Throws(NotFoundException::class)
    override fun resumePlayer(securityContext: SecurityContext): Response {
        player.play()
        return getPlayerState(securityContext)
    }

}