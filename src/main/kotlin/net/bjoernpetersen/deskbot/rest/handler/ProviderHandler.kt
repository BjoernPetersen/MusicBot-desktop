package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.NotFoundException
import net.bjoernpetersen.deskbot.rest.async
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.findProvider
import net.bjoernpetersen.deskbot.rest.model.NamedPlugin
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.plugin.NoSuchSongException
import net.bjoernpetersen.musicbot.spi.plugin.id
import javax.inject.Inject
import javax.inject.Named

class ProviderHandler @Inject constructor(
    private val pluginFinder: PluginFinder,
    @Named("PluginClassLoader")
    private val classLoader: ClassLoader) : HandlerController {

    override fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getProviders", ::getProviders)
        routerFactory.addHandlerByOperationId("searchSong", ::searchSong)
        routerFactory.addHandlerByOperationId("lookupSong", ::lookupSong)
    }

    private fun getProviders(ctx: RoutingContext) {
        ctx.response().end(pluginFinder.providers.map {
            NamedPlugin(it.id, it.subject)
        })
    }

    private fun searchSong(ctx: RoutingContext) {
        ctx.async {
            val id = ctx.pathParam("providerId")!!
            val provider = pluginFinder.findProvider(id, classLoader)

            val query = ctx.queryParam("query").first()
            val offset = ctx.queryParam("offset").firstOrNull()?.toInt() ?: 0
            val limit = ctx.queryParam("limit").firstOrNull()?.toInt()

            // TODO if search gets a limit parameter, this should be changed
            provider.search(query, offset)
                .let { if (limit == null) it else it.subList(0, Math.min(limit, it.size)) }
        } success {
            ctx.response().end(it)
        } failure {
            ctx.fail(it)
        }
    }

    private fun lookupSong(ctx: RoutingContext) {
        ctx.async {
            val providerId = ctx.pathParam("providerId")!!
            val songId = ctx.pathParam("songId")!!
            val provider = pluginFinder.findProvider(providerId, classLoader)
            provider.lookup(songId)
        } success {
            ctx.response().end(it)
        } failure {
            if (it is NoSuchSongException) ctx.fail(NotFoundException())
            else ctx.fail(it)
        }
    }
}
