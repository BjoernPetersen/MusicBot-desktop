package com.github.bjoernpetersen.deskbot.api.swag.api.impl

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException
import com.github.bjoernpetersen.deskbot.api.swag.api.UserApiService
import com.github.bjoernpetersen.deskbot.api.swag.model.PasswordChange
import com.github.bjoernpetersen.deskbot.api.swag.model.RegisterCredentials
import com.github.bjoernpetersen.jmusicbot.MusicBot
import com.github.bjoernpetersen.jmusicbot.user.DuplicateUserException
import com.github.bjoernpetersen.jmusicbot.user.InvalidTokenException
import com.github.bjoernpetersen.jmusicbot.user.User
import com.github.bjoernpetersen.jmusicbot.user.UserManager
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
  override fun registerUser(credentials: RegisterCredentials,
      securityContext: SecurityContext): Response {
    return try {
      val user = userManager.createTemporaryUser(credentials.name, credentials.uuid)
      Response.status(Response.Status.CREATED).entity(userManager.toToken(user)).build()
    } catch (e: DuplicateUserException) {
      Response.status(Response.Status.CONFLICT).build()
    }
  }

  override fun changePassword(authorization: String, change: PasswordChange,
      securityContext: SecurityContext): Response {
    val user: User
    try {
      user = userManager.fromToken(authorization)
    } catch (e: InvalidTokenException) {
      return Response.status(Response.Status.UNAUTHORIZED).build()
    }

    if (!user.isTemporary) {
      val oldPassword = change.oldPassword
      if (oldPassword == null
          || !user.hasPassword(oldPassword)) {
        return Response.status(Response.Status.FORBIDDEN).build()
      }
    }

    return try {
      val newUser = userManager.updateUser(user, change.newPassword)
      Response.ok(userManager.toToken(newUser)).build()
    } catch (e: SQLException) {
      Response.serverError().build()
    } catch (e: DuplicateUserException) {
      Response.serverError().build()
    }

  }

}
