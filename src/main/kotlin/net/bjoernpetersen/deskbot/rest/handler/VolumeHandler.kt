package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.async
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.plugin.volume.VolumeManager
import javax.inject.Inject

class VolumeHandler @Inject private constructor(
    private val volumeManager: VolumeManager) : HandlerController {

    override fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getVolume", ::getVolume)
        routerFactory.addHandlerByOperationId("setVolume", ::setVolume)
    }

    private fun getVolume(ctx: RoutingContext) {
        ctx.async {
            volumeManager.getVolume()
        } success {
            ctx.response().end(it)
        } failure {
            ctx.fail(it)
        }
    }

    private fun setVolume(ctx: RoutingContext) {
        ctx.async {
            ctx.require(Permission.CHANGE_VOLUME)
            val value = ctx.queryParam("value").first().toInt()
            volumeManager.setVolume(value)
            volumeManager.getVolume()
        } success {
            ctx.response().end(it)
        } failure {
            ctx.fail(it)
        }
    }
}
