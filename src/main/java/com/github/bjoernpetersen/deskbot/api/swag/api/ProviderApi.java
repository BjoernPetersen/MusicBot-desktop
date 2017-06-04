package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.api.factories.ProviderApiServiceFactory;
import com.github.bjoernpetersen.deskbot.api.swag.model.Song;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/provider")
@Consumes({"application/json"})
@Produces({"application/json"})
@Api(description = "the provider API")

public class ProviderApi {

  private final ProviderApiService delegate = ProviderApiServiceFactory.getProviderApi();

  @GET

  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Returns a list of all available providers", notes = "", response = String.class, responseContainer = "List", tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A list of unique provider IDs", response = String.class, responseContainer = "List")})
  public Response getProviders(@Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.getProviders(securityContext);
  }

  @GET
  @Path("/{providerId}/{songId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Looks up a song", notes = "Looks up a song using its ID and a provider ID", response = Song.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The looked up song", response = Song.class),

      @ApiResponse(code = 400, message = "A parameter is missing", response = Song.class),

      @ApiResponse(code = 404, message = "The song could not be found", response = Song.class)})
  public Response lookupSong(
      @ApiParam(value = "A song ID", required = true) @PathParam("songId") String songId
      ,
      @ApiParam(value = "A provider ID", required = true) @PathParam("providerId") String providerId
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.lookupSong(songId, providerId, securityContext);
  }

  @GET
  @Path("/{providerId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Searches for songs", notes = "", response = Song.class, responseContainer = "List", tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A list of results", response = Song.class, responseContainer = "List"),

      @ApiResponse(code = 400, message = "A parameter is missing", response = Song.class, responseContainer = "List"),

      @ApiResponse(code = 404, message = "Invalid provider ID", response = Song.class, responseContainer = "List")})
  public Response searchSong(
      @ApiParam(value = "The provider with which to search", required = true) @PathParam("providerId") String providerId
      , @ApiParam(value = "A search query", required = true) @QueryParam("query") String query
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.searchSong(providerId, query, securityContext);
  }
}
