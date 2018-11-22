package net.bjoernpetersen.deskbot.rest

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import io.vertx.kotlin.ext.web.api.contract.routerFactoryOptionsOf
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.rest.handler.BasicSecurityHandler
import net.bjoernpetersen.deskbot.rest.handler.BearerSecurityHandler
import net.bjoernpetersen.deskbot.rest.handler.FailureHandler
import net.bjoernpetersen.deskbot.rest.handler.PlayerHandler
import net.bjoernpetersen.deskbot.rest.handler.ProviderHandler
import net.bjoernpetersen.deskbot.rest.handler.QueueHandler
import net.bjoernpetersen.deskbot.rest.handler.SuggesterHandler
import net.bjoernpetersen.deskbot.rest.handler.UserHandler
import javax.inject.Inject

class RestServer @Inject constructor(
    private val bearerSecurityHandler: BearerSecurityHandler,
    private val basicSecurityHandler: BasicSecurityHandler,
    private val userHandler: UserHandler,
    private val playerHandler: PlayerHandler,
    private val queueHandler: QueueHandler,
    private val providerHandler: ProviderHandler,
    private val suggesterHandler: SuggesterHandler) : AbstractVerticle() {

    private val logger = KotlinLogging.logger {}

    override fun start(startFuture: Future<Void>) {
        registerKotlinJacksonMapper()

        OpenAPI3RouterFactory.create(vertx, "openapi/MusicBot.yaml") { result ->
            if (result.succeeded()) {
                val routerFactory = result.result()!!
                routerFactory.options = routerFactoryOptionsOf(
                    mountNotImplementedHandler = true) // TODO remove

                routerFactory
                    .register(userHandler)
                    .register(playerHandler)
                    .register(queueHandler)
                    .register(providerHandler)
                    .register(suggesterHandler)

                routerFactory.addSecurityHandler("Token", bearerSecurityHandler)
                routerFactory.addSecurityHandler("Basic", basicSecurityHandler)

                val router: Router = routerFactory.router
                router.errorHandler(500) {
                    val error = it.failure()
                    if (error !is StatusException) {
                        logger.error(error) { "An unknown error occurred." }
                    }
                }
                router.route().failureHandler(FailureHandler())

                val serverOptions = HttpServerOptions().apply {
                    // TODO these should be in some config
                    host = "localhost"
                    port = 42945
                }
                val server = vertx.createHttpServer(serverOptions)
                server.requestHandler(router).listen()
                startFuture.complete()
            } else {
                startFuture.fail(result.cause())
            }
        }
    }

    private fun registerKotlinJacksonMapper() {
        KotlinModule().let {
            listOf(Json.mapper, Json.prettyMapper).forEach { mapper ->
                mapper.registerModule(it)
                mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
        }
    }
}

private fun OpenAPI3RouterFactory.register(controller: HandlerController) = this.apply {
    controller.register(this)
}
