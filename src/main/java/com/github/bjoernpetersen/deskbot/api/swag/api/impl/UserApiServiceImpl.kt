package com.github.bjoernpetersen.deskbot.api.swag.api.impl

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException
import com.github.bjoernpetersen.deskbot.api.swag.api.UserApiService
import com.github.bjoernpetersen.jmusicbot.MusicBot
import com.github.bjoernpetersen.jmusicbot.user.*
import java.sql.SQLException
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext


class UserApiServiceImpl : UserApiService() {
    private lateinit var userManager: UserManager

    override fun initialize(bot: MusicBot) {
        userManager = bot.userManager
    }

    @Throws(NotFoundException::class)
    override fun deleteUser(authorization: String, securityContext: SecurityContext): Response {
        val user: User
        try {
            user = userManager.fromToken(authorization)
        } catch (e: InvalidTokenException) {
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }

        try {
            userManager.deleteUser(user)
        } catch (e: SQLException) {
            return Response.serverError().build()
        }

        return Response.noContent().build()
    }

    @Throws(NotFoundException::class)
    override fun login(userName: String, password: String?, uuid: String?,
                       securityContext: SecurityContext): Response {
        val user: User
        try {
            user = userManager.getUser(userName)
        } catch (e: UserNotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).build()
        }

        if (user.isTemporary) {
            if (uuid == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build()
            }
            if (user.hasUuid(uuid)) {
                return Response.ok(userManager.toToken(user)).build()
            } else {
                return Response.status(Response.Status.BAD_REQUEST).build()
            }
        } else {
            if (password == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build()
            }
            if (user.hasPassword(password)) {
                return Response.ok(userManager.toToken(user)).build()
            } else {
                return Response.status(Response.Status.FORBIDDEN).build()
            }
        }
    }

    @Throws(NotFoundException::class)
    override fun registerUser(userName: String, uuid: String, securityContext: SecurityContext): Response {
        try {
            val user = userManager.createTemporaryUser(userName, uuid)
            return Response.status(Response.Status.CREATED).entity(userManager.toToken(user)).build()
        } catch (e: DuplicateUserException) {
            return Response.status(Response.Status.CONFLICT).build()
        }

    }

    override fun changePassword(authorization: String, password: String, oldPassword: String?,
                                securityContext: SecurityContext): Response {
        val user: User
        try {
            user = userManager.fromToken(authorization)
        } catch (e: InvalidTokenException) {
            return Response.status(Response.Status.UNAUTHORIZED).build()
        }

        if (!user.isTemporary) {
            if (oldPassword == null) {
                return Response.status(Response.Status.UNAUTHORIZED).build()
            } else if (!user.hasPassword(oldPassword)) {
                return Response.status(Response.Status.FORBIDDEN).build()
            }
        }

        try {
            val newUser = userManager.updateUser(user, password)
            return Response.ok(userManager.toToken(newUser)).build()
        } catch (e: SQLException) {
            return Response.serverError().build()
        } catch (e: DuplicateUserException) {
            return Response.serverError().build()
        }

    }

}