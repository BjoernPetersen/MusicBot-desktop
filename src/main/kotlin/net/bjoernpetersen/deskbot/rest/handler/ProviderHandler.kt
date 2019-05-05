package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.findProvider
import net.bjoernpetersen.deskbot.rest.model.NamedPlugin
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.plugin.PluginLookup
import net.bjoernpetersen.musicbot.spi.plugin.id
import javax.inject.Inject

class ProviderHandler @Inject constructor(
    private val pluginFinder: PluginFinder,
    private val pluginLookup: PluginLookup
) : HandlerController {

    override suspend fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getProviders", ::getProviders)
        routerFactory.addHandlerByOperationId("searchSong", ::searchSong)
        routerFactory.addHandlerByOperationId("lookupSong", ::lookupSong)
    }

    private fun getProviders(ctx: RoutingContext) {
        ctx.response().end(pluginFinder.providers.map {
            NamedPlugin(it.id, it.subject)
        })
    }

    private suspend fun searchSong(ctx: RoutingContext) {
        val id = ctx.pathParam("providerId")!!
        val provider = pluginLookup.findProvider(id)

        val query = ctx.queryParam("query").first()
        val offset = ctx.queryParam("offset").firstOrNull()?.toInt() ?: 0
        val limit = ctx.queryParam("limit").firstOrNull()?.toInt()

        // TODO if search gets a limit parameter, this should be changed
        val result = provider.search(query, offset)
            .let { if (limit == null) it else it.subList(0, Math.min(limit, it.size)) }
        ctx.response().end(result)
    }

    private suspend fun lookupSong(ctx: RoutingContext) {
        val providerId = ctx.pathParam("providerId")!!
        val songId = ctx.pathParam("songId")!!
        val provider = pluginLookup.findProvider(providerId)
        val song = provider.lookup(songId)
        ctx.response().end(song)
    }
}
