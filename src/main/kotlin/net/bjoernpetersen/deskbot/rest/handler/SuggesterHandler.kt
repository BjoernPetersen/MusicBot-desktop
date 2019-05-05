package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.Status
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.findProvider
import net.bjoernpetersen.deskbot.rest.findSuggester
import net.bjoernpetersen.deskbot.rest.model.NamedPlugin
import net.bjoernpetersen.deskbot.rest.setStatus
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.plugin.PluginLookup
import net.bjoernpetersen.musicbot.spi.plugin.id
import javax.inject.Inject

class SuggesterHandler @Inject constructor(
    private val pluginFinder: PluginFinder,
    private val pluginLookup: PluginLookup
) : HandlerController {

    override suspend fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getSuggesters", ::getSuggesters)
        routerFactory.addHandlerByOperationId("suggestSong", ::suggestSongs)
        routerFactory.addHandlerByOperationId("removeSuggestion", ::removeSuggestion)
    }

    private fun getSuggesters(ctx: RoutingContext) {
        ctx.response().end(pluginFinder.suggesters.map {
            NamedPlugin(it.id, it.subject)
        })
    }

    private suspend fun suggestSongs(ctx: RoutingContext) {
        val suggesterId = ctx.pathParam("suggesterId")!!
        val max = ctx.queryParam("max").firstOrNull()?.toInt() ?: 32
        val suggester = pluginLookup.findSuggester(suggesterId)
        val suggestions = suggester.getNextSuggestions(max)
        ctx.response().end(suggestions)
    }

    private suspend fun removeSuggestion(ctx: RoutingContext) {
        ctx.require(Permission.DISLIKE)
        val suggesterId = ctx.pathParam("suggesterId")!!
        val providerId = ctx.queryParam("providerId").first()
        val songId = ctx.queryParam("songId").first()
        val suggester = pluginLookup.findSuggester(suggesterId)
        val provider = pluginLookup.findProvider(providerId)
        val song = provider.lookup(songId)
        suggester.removeSuggestion(song)
        ctx.response().setStatus(Status.NO_CONTENT).end()
    }
}
