package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.AuthException
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.Status
import net.bjoernpetersen.deskbot.rest.StatusException
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.findProvider
import net.bjoernpetersen.deskbot.rest.model.CoreQueueEntry
import net.bjoernpetersen.deskbot.rest.model.toModel
import net.bjoernpetersen.deskbot.rest.model.tokenExpect
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.spi.player.PlayerHistory
import net.bjoernpetersen.musicbot.spi.player.SongQueue
import net.bjoernpetersen.musicbot.spi.plugin.PluginLookup
import javax.inject.Inject

class QueueHandler @Inject private constructor(
    private val queue: SongQueue,
    private val pluginLookup: PluginLookup,
    private val playerHistory: PlayerHistory
) : HandlerController {

    override suspend fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getQueue", ::getQueue)
        routerFactory.addHandlerByOperationId("getRecentQueue", ::getRecentQueue)
        routerFactory.addHandlerByOperationId("enqueue", ::enqueue)
        routerFactory.addHandlerByOperationId("dequeue", ::dequeue)
        routerFactory.addHandlerByOperationId("moveEntry", ::moveEntry)
    }

    private fun getQueue(ctx: RoutingContext) {
        ctx.response().end(queue.toModel())
    }

    private fun getRecentQueue(ctx: RoutingContext) {
        ctx.response().end(playerHistory.getHistory().toModel())
    }

    private suspend fun enqueue(ctx: RoutingContext) {
        ctx.require(Permission.ENQUEUE)
        val songId = ctx.queryParam("songId").first()!!
        val providerId = ctx.queryParam("providerId").first()!!
        if (songId.isBlank() || providerId.isBlank()) {
            throw StatusException(Status.BAD_REQUEST, "providerId and songId are required")
        }

        val provider = pluginLookup.findProvider(providerId)
        val song = provider.lookup(songId)
        val user = ctx.authUser
        queue.insert(CoreQueueEntry(song, user))
        ctx.response().end(queue.toModel())
    }

    private suspend fun dequeue(ctx: RoutingContext) {
        val songId = ctx.queryParam("songId").first()!!
        val providerId = ctx.queryParam("providerId").first()!!
        if (songId.isBlank() || providerId.isBlank()) {
            throw StatusException(Status.BAD_REQUEST, "providerId and songId are required")
        }

        val hasPermission = ctx.authUser.permissions.contains(Permission.SKIP)
            || queue.toList().firstOrNull { it.song.id == songId }?.let { it.user == ctx.authUser }
            ?: return ctx.response().end(queue.toModel())

        if (!hasPermission) {
            throw AuthException(Status.FORBIDDEN, tokenExpect(listOf(Permission.SKIP)))
        }

        val provider = pluginLookup.findProvider(providerId)
        val song = provider.lookup(songId)
        queue.remove(song)
        ctx.response().end(queue.toModel())
    }

    private suspend fun moveEntry(ctx: RoutingContext) {
        ctx.require(Permission.MOVE)

        val providerId = ctx.queryParam("providerId").first()
        val provider = pluginLookup.findProvider(providerId)

        val songId = ctx.queryParam("songId").first()
        val song = provider.lookup(songId)

        val index = ctx.queryParam("index").first().toInt()
        queue.move(song, index)
        ctx.response().end(queue.toModel())
    }
}
