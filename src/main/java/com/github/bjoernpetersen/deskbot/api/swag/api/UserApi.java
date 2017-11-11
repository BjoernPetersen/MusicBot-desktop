package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.api.factories.UserApiServiceFactory;
import com.github.bjoernpetersen.deskbot.api.swag.model.PasswordChange;
import com.github.bjoernpetersen.deskbot.api.swag.model.RegisterCredentials;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/user")
@Consumes({"application/json"})
@Produces({"application/json"})
@Api(description = "the user API")

public class UserApi {

  private final UserApiService delegate = UserApiServiceFactory.getUserApi();

  @PUT

  @Consumes({"application/json"})
  @Produces({"text/plain; charset=utf-8"})
  @ApiOperation(value = "Sets a new password", notes = "Sets a new password for the caller. If the user was a guest account, this makes him a full user.", response = String.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A new Authorization token", response = String.class),

      @ApiResponse(code = 400, message = "Invalid new password", response = String.class),

      @ApiResponse(code = 401, message = "Invalid or missing token", response = String.class),

      @ApiResponse(code = 403, message = "Wrong old password", response = String.class)})
  public Response changePassword(
      @ApiParam(value = "An authorization token", required = true) @HeaderParam("Authorization") String authorization
      ,
      @ApiParam(value = "The users old password (if he's no guest) and new password.", required = true) PasswordChange passwordChange
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.changePassword(authorization, passwordChange, securityContext);
  }

  @DELETE

  @Consumes({"application/json"})
  @Produces({"text/plain; charset=utf-8"})
  @ApiOperation(value = "Deletes a user", notes = "Deletes the user associated with the Authorization token.", response = void.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Successfully deleted", response = void.class),

      @ApiResponse(code = 401, message = "Invalid or missing token", response = void.class)})
  public Response deleteUser(
      @ApiParam(value = "An authorization token", required = true) @HeaderParam("Authorization") String authorization
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.deleteUser(authorization, securityContext);
  }

  @POST

  @Consumes({"application/json"})
  @Produces({"text/plain; charset=utf-8"})
  @ApiOperation(value = "Registers a new user", notes = "Adds a new guest user to the database. The user is identified by his username.", response = String.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 201, message = "An authorization token", response = String.class),

      @ApiResponse(code = 409, message = "Username already in use", response = String.class)})
  public Response registerUser(
      @ApiParam(value = "The new user's credentials.", required = true) RegisterCredentials credentials
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.registerUser(credentials, securityContext);
  }
}
