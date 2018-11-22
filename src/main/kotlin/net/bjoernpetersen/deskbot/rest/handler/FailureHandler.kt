package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.rest.StatusException
import net.bjoernpetersen.deskbot.rest.end

class FailureHandler : Handler<RoutingContext> {

    private val logger = KotlinLogging.logger { }

    override fun handle(ctx: RoutingContext) {
        val failure = ctx.failure()
        when (failure) {
            null -> ctx.next()
            is StatusException ->
                if (failure.body == null)
                    ctx.response().setStatusCode(failure.status.code).end()
                else
                    ctx.response().setStatusCode(failure.status.code).end(failure.body)
            else -> {
                logger.error(failure) { "Unknown error occurred" }
                ctx.next()
            }
        }
    }
}
