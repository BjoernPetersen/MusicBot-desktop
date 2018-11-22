package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.async
import net.bjoernpetersen.deskbot.rest.bodyAs
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.model.PlayerStateAction
import net.bjoernpetersen.deskbot.rest.model.PlayerStateChange
import net.bjoernpetersen.deskbot.rest.model.toModel
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.spi.player.Player
import javax.inject.Inject

class PlayerHandler @Inject constructor(private val player: Player) : HandlerController {

    override fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getPlayerState", ::getPlayerState)
        routerFactory.addHandlerByOperationId("setPlayerState", ::setPlayerState)
    }

    private fun getPlayerState(ctx: RoutingContext) {
        ctx.response().end(player.state.toModel())
    }

    private fun resume(ctx: RoutingContext) {
        ctx.async {
            ctx.require(Permission.PAUSE)
            player.play()
        } success {
            getPlayerState(ctx)
        } failure { ctx.fail(it) }
    }

    private fun pause(ctx: RoutingContext) {
        ctx.async {
            ctx.require(Permission.PAUSE)
            player.pause()
        } success {
            getPlayerState(ctx)
        } failure { ctx.fail(it) }
    }

    private fun skip(ctx: RoutingContext) {
        ctx.async {
            ctx.require(Permission.SKIP)
            player.next()
        } success {
            getPlayerState(ctx)
        } failure { ctx.fail(it) }
    }

    private fun setPlayerState(ctx: RoutingContext) {
        when (ctx.bodyAs<PlayerStateChange>().action) {
            PlayerStateAction.PLAY -> resume(ctx)
            PlayerStateAction.PAUSE -> pause(ctx)
            PlayerStateAction.SKIP -> skip(ctx)
        }
    }
}
