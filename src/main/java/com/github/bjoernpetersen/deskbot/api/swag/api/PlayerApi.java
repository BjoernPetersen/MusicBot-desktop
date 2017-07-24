package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.api.factories.PlayerApiServiceFactory;
import com.github.bjoernpetersen.deskbot.api.swag.model.PlayerState;
import com.github.bjoernpetersen.deskbot.api.swag.model.QueueEntry;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@Path("/player")
@Consumes({"application/json"})
@Produces({"application/json"})
@Api(description = "the player API")

public class PlayerApi {

  private final PlayerApiService delegate = PlayerApiServiceFactory.getPlayerApi();

  @DELETE
  @Path("/queue")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Removes a Song from the queue", notes = "Removes the specified Song from the current queue. If the queue did not contain the song, nothing is done.", response = QueueEntry.class, responseContainer = "List", tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The new queue", response = QueueEntry.class, responseContainer = "List"),

      @ApiResponse(code = 400, message = "A parameter is missing", response = QueueEntry.class, responseContainer = "List"),

      @ApiResponse(code = 401, message = "Invalid or missing Authorization token", response = QueueEntry.class, responseContainer = "List"),

      @ApiResponse(code = 403, message = "Not authorized", response = QueueEntry.class, responseContainer = "List"),

      @ApiResponse(code = 404, message = "The song could not be found", response = QueueEntry.class, responseContainer = "List")})
  public Response dequeue(
      @ApiParam(value = "Authorization token with 'skip' permission", required = true) @HeaderParam("Authorization") String authorization
      ,
      @ApiParam(value = "the song ID of the song to dequeue", required = true) @QueryParam("songId") String songId
      ,
      @ApiParam(value = "the provider ID of the song to dequeue", required = true) @QueryParam("providerId") String providerId
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.dequeue(authorization, songId, providerId, securityContext);
  }

  @PUT
  @Path("/queue")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Adds a Song to the queue", notes = "Adds the specified Song to the current queue. If the queue already contains the Song, it won't be added.", response = QueueEntry.class, responseContainer = "List", tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "The new queue", response = QueueEntry.class, responseContainer = "List"),

      @ApiResponse(code = 400, message = "A parameter is missing", response = QueueEntry.class, responseContainer = "List"),

      @ApiResponse(code = 401, message = "Invalid or missing Authorization token", response = QueueEntry.class, responseContainer = "List"),

      @ApiResponse(code = 404, message = "The song could not be found", response = QueueEntry.class, responseContainer = "List")})
  public Response enqueue(
      @ApiParam(value = "Authorization token", required = true) @HeaderParam("Authorization") String authorization
      , @ApiParam(value = "The song's ID", required = true) @QueryParam("songId") String songId
      ,
      @ApiParam(value = "The ID of the provider the song is from", required = true) @QueryParam("providerId") String providerId
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.enqueue(authorization, songId, providerId, securityContext);
  }

  @GET

  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Returns the current player state", notes = "Returns the current player state. If the state is PLAY or PAUSE, it also contains the current song.", response = PlayerState.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A PlayerState", response = PlayerState.class)})
  public Response getPlayerState(@Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.getPlayerState(securityContext);
  }

  @GET
  @Path("/queue")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Returns the current player queue", notes = "", response = QueueEntry.class, responseContainer = "List", tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A list of Songs", response = QueueEntry.class, responseContainer = "List")})
  public Response getQueue(@Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.getQueue(securityContext);
  }

  @PUT
  @Path("/next")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Skips to the next song", notes = "Skips the current song and plays the next song.", response = PlayerState.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A PlayerState", response = PlayerState.class),

      @ApiResponse(code = 401, message = "Invalid or missing Authorization token", response = PlayerState.class),

      @ApiResponse(code = 403, message = "Not authorized", response = PlayerState.class)})
  public Response nextSong(
      @ApiParam(value = "Authorization token with 'skip' permission", required = true) @HeaderParam("Authorization") String authorization
      , @Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.nextSong(authorization, securityContext);
  }

  @PUT
  @Path("/pause")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Pauses the player", notes = "Pauses the current playback. If the current player state is not PLAY, does nothing.", response = PlayerState.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A PlayerState", response = PlayerState.class)})
  public Response pausePlayer(@Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.pausePlayer(securityContext);
  }

  @PUT
  @Path("/play")
  @Consumes({"application/json"})
  @Produces({"application/json"})
  @ApiOperation(value = "Resumes the player", notes = "Pauses the current playback. If the current player state is not PAUSE, does nothing.", response = PlayerState.class, tags = {})
  @ApiResponses(value = {
      @ApiResponse(code = 200, message = "A PlayerState", response = PlayerState.class)})
  public Response resumePlayer(@Context SecurityContext securityContext)
      throws NotFoundException {
    return delegate.resumePlayer(securityContext);
  }
}
