package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.api.factories.SuggesterApiServiceFactory;
import com.github.bjoernpetersen.deskbot.api.swag.model.NamedPlugin;
import com.github.bjoernpetersen.deskbot.api.swag.model.Song;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/suggester")
@Consumes({"application/json"})
@Produces({"application/json"})
@Api(description = "the suggester API")

public class SuggesterApi {

  private final SuggesterApiService delegate = SuggesterApiServiceFactory.getSuggesterApi();

  @GET

  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Returns a list of all available suggesters", notes = "", response = NamedPlugin.class, responseContainer = "List", tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A list of unique suggester IDs", response = NamedPlugin.class, responseContainer = "List")})
  public Response getSuggesters(@Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.getSuggesters(securityContext);
  }

  @DELETE
  @Path("/{suggesterId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Removes a song from the suggestions", notes = "", response = void.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 204, message = "Success. Will also be the case if the song was not in the current suggestions.", response = void.class),

      @ApiResponse(code = 401, message = "Invalid or missing Authorization token", response = void.class),

      @ApiResponse(code = 403, message = "Missing 'dislike' permission", response = void.class),

      @ApiResponse(code = 404, message = "Song or Provider not found.", response = void.class)})
  public Response removeSuggestion(
      @ApiParam(value = "the ID of the suggester", required = true) @PathParam("suggesterId") String suggesterId
      ,
      @ApiParam(value = "An authorization token with 'dislike' permission", required = true) @HeaderParam("Authorization") String authorization
      ,
      @ApiParam(value = "The ID of the song to remove", required = true) @QueryParam("songId") String songId
      ,
      @ApiParam(value = "The ID of the provider of the song to remove", required = true) @QueryParam("providerId") String providerId
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate
        .removeSuggestion(suggesterId, authorization, songId, providerId, securityContext);
  }

  @GET
  @Path("/{suggesterId}")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Returns a list of suggestions", notes = "", response = Song.class, responseContainer = "List", tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A list of suggested songs", response = Song.class, responseContainer = "List"),

      @ApiResponse(code = 404, message = "Suggester not found", response = Song.class, responseContainer = "List")})
  public Response suggestSong(
      @ApiParam(value = "A suggester ID", required = true) @PathParam("suggesterId") String suggesterId
      ,
      @ApiParam(value = "The maximum size of the response. Defaults to 32.", defaultValue = "32") @DefaultValue("32") @QueryParam("max") Integer max
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.suggestSong(suggesterId, max, securityContext);
  }
}
