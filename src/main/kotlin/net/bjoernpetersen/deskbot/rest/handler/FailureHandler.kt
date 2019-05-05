package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.rest.Status
import net.bjoernpetersen.deskbot.rest.StatusException
import net.bjoernpetersen.deskbot.rest.end
import net.bjoernpetersen.deskbot.rest.setStatus
import net.bjoernpetersen.musicbot.spi.plugin.NoSuchSongException

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
            is NoSuchSongException ->
                // Spare handlers one try-catch-rethrow
                ctx.response().setStatus(Status.NOT_FOUND).end()
            else -> {
                logger.error(failure) { "Unknown error occurred" }
                ctx.next()
            }
        }
    }
}
