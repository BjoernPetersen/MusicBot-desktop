package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import javafx.application.Platform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.Status
import net.bjoernpetersen.deskbot.rest.setStatus
import net.bjoernpetersen.musicbot.api.auth.Permission
import javax.inject.Inject

class ExitHandler @Inject private constructor() : HandlerController {

    private val logger = KotlinLogging.logger { }

    override suspend fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("exit", ::exit)
    }

    private fun exit(ctx: RoutingContext) {
        ctx.require(Permission.EXIT)
        ctx.response().setStatus(Status.NO_CONTENT).end()
        GlobalScope.launch(Dispatchers.Main) {
            delay(500)
            logger.info { "Closing due to remote user request" }
            Platform.exit()
        }
    }
}
