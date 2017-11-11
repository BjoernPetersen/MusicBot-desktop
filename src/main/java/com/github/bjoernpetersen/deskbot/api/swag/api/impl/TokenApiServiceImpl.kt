package com.github.bjoernpetersen.deskbot.api.swag.api.impl

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException
import com.github.bjoernpetersen.deskbot.api.swag.api.TokenApiService
import com.github.bjoernpetersen.deskbot.api.swag.model.LoginCredentials
import com.github.bjoernpetersen.jmusicbot.MusicBot
import com.github.bjoernpetersen.jmusicbot.user.User
import com.github.bjoernpetersen.jmusicbot.user.UserManager
import com.github.bjoernpetersen.jmusicbot.user.UserNotFoundException
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext


class TokenApiServiceImpl : TokenApiService() {
  private lateinit var userManager: UserManager

  override fun initialize(bot: MusicBot) {
    userManager = bot.userManager
  }

  @Throws(NotFoundException::class)
  override fun login(credentials: LoginCredentials, securityContext: SecurityContext): Response {
    val user: User
    try {
      user = userManager.getUser(credentials.name)
    } catch (e: UserNotFoundException) {
      return Response.status(Response.Status.NOT_FOUND).build()
    }

    if (user.isTemporary) {
      val uuid = credentials.uuid ?: return Response.status(Response.Status.UNAUTHORIZED).build()
      return if (user.hasUuid(uuid)) {
        Response.ok(userManager.toToken(user)).build()
      } else {
        Response.status(Response.Status.BAD_REQUEST).build()
      }
    } else {
      val password = credentials.password ?:
          return Response.status(Response.Status.UNAUTHORIZED).build()
      return if (user.hasPassword(password)) {
        Response.ok(userManager.toToken(user)).build()
      } else {
        Response.status(Response.Status.FORBIDDEN).build()
      }
    }
  }
}
