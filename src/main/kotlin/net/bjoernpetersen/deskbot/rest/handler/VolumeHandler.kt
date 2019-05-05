package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.plugin.volume.VolumeManager
import javax.inject.Inject

class VolumeHandler @Inject private constructor(
    private val volumeManager: VolumeManager
) : HandlerController {

    override suspend fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getVolume", ::getVolume)
        routerFactory.addHandlerByOperationId("setVolume", ::setVolume)
    }

    private suspend fun getVolume(ctx: RoutingContext) {
        val volume = volumeManager.getVolume()
        ctx.response().end(volume)
    }

    private suspend fun setVolume(ctx: RoutingContext) {
        ctx.require(Permission.CHANGE_VOLUME)
        val value = ctx.queryParam("value").first().toInt()
        volumeManager.setVolume(value)
        getVolume(ctx)
    }
}
