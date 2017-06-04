package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.api.factories.SuggesterApiServiceFactory;
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

@Path("/suggester")
@Consumes({"application/json"})
@Produces({"application/json"})
@Api(description = "the suggester API")

public class SuggesterApi {

  private final SuggesterApiService delegate = SuggesterApiServiceFactory.getSuggesterApi();

  @GET

  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Returns a list of all available suggesters", notes = "", response = String.class, responseContainer = "List", tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A list of unique suggester IDs", response = String.class, responseContainer = "List")})
  public Response getSuggesters(@Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.getSuggesters(securityContext);
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
      @ApiParam(value = "The maximum size of the response. Defaults to 16.") @QueryParam("max") Integer max
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.suggestSong(suggesterId, max, securityContext);
  }
}
