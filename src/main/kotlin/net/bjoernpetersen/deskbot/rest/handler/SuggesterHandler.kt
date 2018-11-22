package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.Status
import net.bjoernpetersen.deskbot.rest.async
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.findProvider
import net.bjoernpetersen.deskbot.rest.findSuggester
import net.bjoernpetersen.deskbot.rest.model.NamedPlugin
import net.bjoernpetersen.deskbot.rest.setStatus
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.plugin.id
import javax.inject.Inject
import javax.inject.Named

class SuggesterHandler @Inject constructor(
    private val pluginFinder: PluginFinder,
    @Named("PluginClassLoader")
    private val classLoader: ClassLoader) : HandlerController {

    override fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("getSuggesters", ::getSuggesters)
        routerFactory.addHandlerByOperationId("suggestSong", ::suggestSongs)
        routerFactory.addHandlerByOperationId("removeSuggestion", ::removeSuggestion)
    }

    private fun getSuggesters(ctx: RoutingContext) {
        ctx.response().end(pluginFinder.suggesters.map {
            NamedPlugin(it.id, it.subject)
        })
    }

    private fun suggestSongs(ctx: RoutingContext) {
        ctx.async {
            val suggesterId = ctx.pathParam("suggesterId")!!
            val max = ctx.pathParam("max").toInt()
            val suggester = pluginFinder.findSuggester(suggesterId, classLoader)
            suggester.getNextSuggestions(max)
        } success {
            ctx.response().end(it)
        } failure {
            if (it is NumberFormatException) {
                ctx.response().setStatus(Status.BAD_REQUEST).end()
            } else ctx.fail(it)
        }
    }

    private fun removeSuggestion(ctx: RoutingContext) {
        ctx.async {
            ctx.require(Permission.DISLIKE)
            val suggesterId = ctx.pathParam("suggesterId")!!
            val providerId = ctx.pathParam("providerId")!!
            val songId = ctx.pathParam("songId")!!
            val suggester = pluginFinder.findSuggester(suggesterId, classLoader)
            val provider = pluginFinder.findProvider(providerId, classLoader)
            val song = provider.lookup(songId)
            suggester.removeSuggestion(song)
        } success {
            ctx.response().setStatus(Status.NO_CONTENT).end()
        } failure {
            ctx.fail(it)
        }
    }
}
