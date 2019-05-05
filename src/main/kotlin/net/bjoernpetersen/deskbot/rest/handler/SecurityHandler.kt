package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.core.AsyncResult
import io.vertx.core.Future
import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.AuthProvider
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.rest.AuthException
import net.bjoernpetersen.deskbot.rest.Status
import net.bjoernpetersen.deskbot.rest.model.AuthExpectation
import net.bjoernpetersen.deskbot.rest.model.AuthType
import net.bjoernpetersen.deskbot.rest.model.UserType
import net.bjoernpetersen.musicbot.api.auth.BotUser
import net.bjoernpetersen.musicbot.api.auth.FullUser
import net.bjoernpetersen.musicbot.api.auth.GuestUser
import net.bjoernpetersen.musicbot.api.auth.InvalidTokenException
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.auth.User
import net.bjoernpetersen.musicbot.api.auth.UserManager
import net.bjoernpetersen.musicbot.api.auth.UserNotFoundException
import java.nio.charset.StandardCharsets
import java.util.Base64
import javax.inject.Inject

class BasicSecurityHandler @Inject constructor(
    private val userManager: UserManager) : Handler<RoutingContext> {

    private val logger = KotlinLogging.logger {}
    override fun handle(ctx: RoutingContext) {
        logger.debug { "Received basic auth request" }
        val auth = ctx.request().getHeader(HEADER)
        if (auth.isNullOrBlank() || !auth.startsWith(PREFIX)) {
            logger.debug { "Received basic auth with invalid format" }
            return ctx.fail(AuthException(Status.UNAUTHORIZED, AuthExpectation(AuthType.Basic)))
        }

        val token = auth.substring(PREFIX.length)
        val decoded = try {
            String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8)
        } catch (e: IllegalArgumentException) {
            logger.debug(e) { "Received invalid basic auth header" }
            return ctx.fail(AuthException(Status.UNAUTHORIZED, AuthExpectation(AuthType.Basic)))
        }

        decoded.split(':').let {
            val userName = it[0]
            val password = it.subList(1, it.size).joinToString(":")
            val dbUser = try {
                userManager.getUser(userName)
            } catch (e: UserNotFoundException) {
                logger.debug(e) { "Tried to log in with unknown user $userName" }
                return ctx.fail(AuthException(Status.UNAUTHORIZED, AuthExpectation(AuthType.Basic)))
            }

            if (!dbUser.hasPassword(password)) {
                logger.debug("Wrong password login attempt")
                val userType = when (dbUser) {
                    is FullUser, BotUser -> UserType.Full
                    is GuestUser -> UserType.Guest
                }
                return ctx.fail(AuthException(Status.UNAUTHORIZED,
                    AuthExpectation(AuthType.Basic, userType)))
            }

            ctx.setUser(WrappedUser(dbUser))
            ctx.next()
        }
    }

    private companion object {
        const val HEADER = "Authorization"
        const val PREFIX = "Basic "
    }
}

class BearerSecurityHandler @Inject constructor(
    private val userManager: UserManager) : Handler<RoutingContext> {

    private val logger = KotlinLogging.logger {}

    override fun handle(ctx: RoutingContext) {
        val bearer = ctx.request().getHeader(HEADER)
        if (bearer.isNullOrBlank() || !bearer.startsWith(PREFIX)) {
            logger.debug { "Received token with invalid format" }
            return ctx.fail(AuthException(Status.UNAUTHORIZED, AuthExpectation(AuthType.Token)))
        }

        val token = bearer.substring(PREFIX.length)
        val user = try {
            userManager.fromToken(token)
        } catch (e: InvalidTokenException) {
            logger.debug(e) { "Received invalid JWT token" }
            return ctx.fail(AuthException(Status.UNAUTHORIZED, AuthExpectation(AuthType.Token)))
        }

        ctx.setUser(WrappedUser(user))
        ctx.next()
    }

    private companion object {
        const val HEADER = "Authorization"
        const val PREFIX = "Bearer "
    }
}
private typealias VertxUser = io.vertx.ext.auth.User

data class WrappedUser(val user: User) : VertxUser {
    override fun clearCache(): VertxUser = this

    override fun setAuthProvider(authProvider: AuthProvider) {}

    override fun isAuthorized(
        authority: String,
        resultHandler: Handler<AsyncResult<Boolean>>): VertxUser {
        val future: Future<Boolean> = Future.future()
        resultHandler.handle(future)

        try {
            val permission = Permission.matchByLabel(authority)
            future.complete(permission in user.permissions)
        } catch (e: IllegalArgumentException) {
            future.fail(e)
        }

        return this
    }

    override fun principal(): JsonObject {
        return json {
            obj()
        }
    }
}

val RoutingContext.authUser: User
    get() = (user() as WrappedUser).user

