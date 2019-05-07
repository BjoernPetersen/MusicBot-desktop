package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.bodyAs
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.model.PlayerStateAction
import net.bjoernpetersen.deskbot.rest.model.PlayerStateChange
import net.bjoernpetersen.deskbot.rest.model.toModel
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.player.ProgressTracker
import net.bjoernpetersen.musicbot.spi.player.Player
import javax.inject.Inject

class PlayerHandler @Inject private constructor(
    private val player: Player,
    private val progressTracker: ProgressTracker
) : HandlerController {

    private val logger = KotlinLogging.logger {}

    override suspend fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getPlayerState", ::getPlayerState)
        routerFactory.addHandlerByOperationId("setPlayerState", ::setPlayerState)
    }

    private fun getPlayerState(ctx: RoutingContext) {
        var progress = runBlocking {
            progressTracker.getCurrentProgress().duration.seconds.toInt()
        }

        if (progress < 0) {
            logger.warn { "Got negative progress from tracker" }
            progress = 0
        }

        val state = player.state
        val songDuration = state.entry?.song?.duration
        if (songDuration != null && progress > songDuration) {
            progress = songDuration
        }

        ctx.response().end(player.state.toModel(progress))
    }

    private suspend fun resume(ctx: RoutingContext) {
        ctx.require(Permission.PAUSE)
        player.play()
        getPlayerState(ctx)
    }

    private suspend fun pause(ctx: RoutingContext) {
        ctx.require(Permission.PAUSE)
        player.pause()
        getPlayerState(ctx)
    }

    private suspend fun skip(ctx: RoutingContext) {
        ctx.require(Permission.SKIP)
        player.next()
        getPlayerState(ctx)
    }

    private suspend fun setPlayerState(ctx: RoutingContext) {
        when (ctx.bodyAs<PlayerStateChange>().action) {
            PlayerStateAction.PLAY -> resume(ctx)
            PlayerStateAction.PAUSE -> pause(ctx)
            PlayerStateAction.SKIP -> skip(ctx)
        }
    }
}
