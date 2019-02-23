package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.NotFoundException
import net.bjoernpetersen.deskbot.rest.Status
import net.bjoernpetersen.deskbot.rest.StatusException
import net.bjoernpetersen.deskbot.rest.async
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.findProvider
import net.bjoernpetersen.deskbot.rest.model.CoreQueueEntry
import net.bjoernpetersen.deskbot.rest.model.toModel
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.player.PlayerHistory
import net.bjoernpetersen.musicbot.spi.player.SongQueue
import net.bjoernpetersen.musicbot.spi.plugin.NoSuchSongException
import javax.inject.Inject
import javax.inject.Named

class QueueHandler @Inject private constructor(
    private val queue: SongQueue,
    private val pluginFinder: PluginFinder,
    @Named("PluginClassLoader")
    private val classLoader: ClassLoader,
    private val playerHistory: PlayerHistory) : HandlerController {

    override fun register(routerFactory: OpenAPI3RouterFactory) {
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

    private fun enqueue(ctx: RoutingContext) {
        ctx.async {
            ctx.require(Permission.ENQUEUE)
            val songId = ctx.queryParam("songId").first()!!
            val providerId = ctx.queryParam("providerId").first()!!
            if (songId.isBlank() || providerId.isBlank()) {
                throw StatusException(Status.BAD_REQUEST, "providerId and songId are required")
            }

            val provider = pluginFinder.findProvider(providerId, classLoader)
            val song = provider.lookup(songId)
            val user = ctx.authUser
            queue.insert(CoreQueueEntry(song, user))
            queue.toModel()
        } success {
            ctx.response().end(it)
        } failure {
            if (it is NoSuchSongException) {
                ctx.fail(NotFoundException())
            } else {
                ctx.fail(it)
            }
        }
    }

    private fun dequeue(ctx: RoutingContext) {
        ctx.async {
            ctx.require(Permission.SKIP)
            val songId = ctx.queryParam("songId").first()!!
            val providerId = ctx.queryParam("providerId").first()!!
            if (songId.isBlank() || providerId.isBlank()) {
                throw StatusException(Status.BAD_REQUEST, "providerId and songId are required")
            }

            val provider = pluginFinder.findProvider(providerId, classLoader)
            val song = provider.lookup(songId)
            queue.remove(song)
            queue.toModel()
        } success {
            ctx.response().end(it)
        } failure {
            if (it is NoSuchSongException) {
                ctx.fail(NotFoundException())
            } else {
                ctx.fail(it)
            }
        }
    }

    private fun moveEntry(ctx: RoutingContext) {
        ctx.async {
            ctx.require(Permission.MOVE)

            val providerId = ctx.queryParam("providerId").first()
            val provider = pluginFinder.findProvider(providerId, classLoader)

            val songId = ctx.queryParam("songId").first()
            val song = provider.lookup(songId)

            val index = ctx.queryParam("index").first().toInt()
            queue.move(song, index)
            queue.toModel()
        } success {
            ctx.response().end(it)
        } failure {
            if (it is NumberFormatException) {
                ctx.fail(StatusException(Status.BAD_REQUEST, it.message))
            } else ctx.fail(it)
        }
    }
}
