package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.api.factories.TokenApiServiceFactory;
import com.github.bjoernpetersen.deskbot.api.swag.model.LoginCredentials;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/token")
@Consumes({"application/json"})
@Produces({"application/json"})
@Api(description = "the token API")

public class TokenApi {

  private final TokenApiService delegate = TokenApiServiceFactory.getTokenApi();

  @PUT

  @Consumes({"application/json"})
  @Produces({"text/plain; charset=utf-8"})
  @ApiOperation(value = "Retrieves a token for a user", notes = "Retrieves an Authorization token for a user. Either a password or a UUID must be supplied. Not both.", response = String.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "An authorization token", response = String.class),

      @ApiResponse(code = 400, message = "Wrong uuid", response = String.class),

      @ApiResponse(code = 401, message = "Needs password or uuid parameter", response = String.class),

      @ApiResponse(code = 403, message = "Wrong password", response = String.class),

      @ApiResponse(code = 404, message = "Unknown user", response = String.class)})
  public Response login(
      @ApiParam(value = "The user credentials to log in with.", required = true) LoginCredentials credentials
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.login(credentials, securityContext);
  }
}
