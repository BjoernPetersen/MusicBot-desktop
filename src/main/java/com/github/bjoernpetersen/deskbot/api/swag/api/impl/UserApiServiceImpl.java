package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException;
import com.github.bjoernpetersen.deskbot.api.swag.api.UserApiService;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.user.DuplicateUserException;
import com.github.bjoernpetersen.jmusicbot.user.InvalidTokenException;
import com.github.bjoernpetersen.jmusicbot.user.User;
import com.github.bjoernpetersen.jmusicbot.user.UserManager;
import com.github.bjoernpetersen.jmusicbot.user.UserNotFoundException;
import java.sql.SQLException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

public class UserApiServiceImpl extends UserApiService {

  private UserManager userManager;

  @Override
  public Response deleteUser(String authorization, SecurityContext securityContext)
      throws NotFoundException {
    User user;
    try {
      user = userManager.fromToken(authorization);
    } catch (InvalidTokenException e) {
      return Response.status(Status.UNAUTHORIZED).build();
    }
    try {
      userManager.deleteUser(user);
    } catch (SQLException e) {
      return Response.serverError().build();
    }
    return Response.noContent().build();
  }

  @Override
  public Response login(String userName, String password, String uuid,
      SecurityContext securityContext) throws NotFoundException {
    User user;
    try {
      user = userManager.getUser(userName);
    } catch (UserNotFoundException e) {
      return Response.status(Status.NOT_FOUND).build();
    }

    if (user.isTemporary()) {
      if (uuid == null) {
        return Response.status(Status.UNAUTHORIZED).build();
      }
      if (user.hasUuid(uuid)) {
        return Response.ok(userManager.toToken(user)).build();
      } else {
        return Response.status(Status.FORBIDDEN).build();
      }
    } else {
      if (password == null) {
        return Response.status(Status.UNAUTHORIZED).build();
      }
      if (user.hasPassword(password)) {
        return Response.ok(userManager.toToken(user)).build();
      } else {
        return Response.status(Status.FORBIDDEN).build();
      }
    }
  }

  @Override
  public Response registerUser(String userName, String uuid, SecurityContext securityContext)
      throws NotFoundException {
    try {
      User user = userManager.createTemporaryUser(userName, uuid);
      return Response.status(Status.CREATED).entity(userManager.toToken(user)).build();
    } catch (DuplicateUserException e) {
      return Response.status(Status.CONFLICT).build();
    }
  }

  @Override
  public Response changePassword(String authorization, String password, String oldPassword,
      SecurityContext securityContext) {
    User user;
    try {
      user = userManager.fromToken(authorization);
    } catch (InvalidTokenException e) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    if (!user.isTemporary()) {
      if (oldPassword == null) {
        return Response.status(Status.UNAUTHORIZED).build();
      } else if (!user.hasPassword(oldPassword)) {
        return Response.status(Status.FORBIDDEN).build();
      }
    }

    try {
      User newUser = userManager.updateUser(user, password);
      return Response.ok(userManager.toToken(newUser)).build();
    } catch (SQLException | DuplicateUserException e) {
      return Response.serverError().build();
    }
  }

  @Override
  public void initialize(MusicBot bot) {
    this.userManager = bot.getUserManager();
  }
}
