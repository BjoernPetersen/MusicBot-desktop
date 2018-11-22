package net.bjoernpetersen.deskbot.rest.handler

import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.api.contract.openapi3.OpenAPI3RouterFactory
import net.bjoernpetersen.deskbot.rest.HandlerController
import net.bjoernpetersen.deskbot.rest.Status
import net.bjoernpetersen.deskbot.rest.async
import net.bjoernpetersen.deskbot.rest.bodyAs
import net.bjoernpetersen.deskbot.rest.model.PasswordChange
import net.bjoernpetersen.deskbot.rest.model.RegisterCredentials
import net.bjoernpetersen.deskbot.rest.setStatus
import net.bjoernpetersen.musicbot.api.auth.DuplicateUserException
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.auth.UserManager
import javax.inject.Inject

class UserHandler @Inject constructor(
    private val userManager: UserManager) : HandlerController {

    override fun register(routerFactory: OpenAPI3RouterFactory) {
        routerFactory.addHandlerByOperationId("registerUser", ::registerUser)
        routerFactory.addHandlerByOperationId("changePassword", ::changePassword)
        routerFactory.addHandlerByOperationId("loginUser", ::loginUser)
        routerFactory.addHandlerByOperationId("deleteUser", ::deleteUser)
    }

    private fun registerUser(ctx: RoutingContext) {
        ctx.async {
            val creds = ctx.bodyAs<RegisterCredentials>()
            val user = userManager.createTemporaryUser(creds.name, creds.userId)
            userManager.toToken(user)
        } success {
            ctx.response().setStatus(Status.CREATED).end(it)
        } failure {
            if (it is DuplicateUserException) ctx.response().setStatus(Status.CONFLICT).end()
            else ctx.fail(it)
        }
    }

    private fun changePassword(ctx: RoutingContext) {
        ctx.async {
            val user = ctx.authUser
            val newPass = ctx.bodyAs<PasswordChange>()
            val new = userManager.updateUser(user, newPass.newPassword)
            // TODO remove
            userManager.updateUser(new, Permission.values().toSet())
            userManager.toToken(new)
        } success {
            ctx.response().end(it)
        } failure {
            ctx.fail(it)
        }
    }

    private fun loginUser(ctx: RoutingContext) {
        ctx.async {
            val user = ctx.authUser
            userManager.toToken(user)
        } success {
            ctx.response().end(it)
        } failure {
            ctx.fail(it)
        }
    }

    private fun deleteUser(ctx: RoutingContext) {
        ctx.async {
            val user = ctx.authUser
            userManager.deleteUser(user)
        } success {
            ctx.response().setStatus(Status.NO_CONTENT).end()
        } failure {
            ctx.fail(it)
        }
    }
}
