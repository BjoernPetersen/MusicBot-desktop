package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.model.*;
import com.github.bjoernpetersen.deskbot.api.swag.api.PlayerApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.factories.PlayerApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import com.github.bjoernpetersen.deskbot.api.swag.model.PlayerState;
import com.github.bjoernpetersen.deskbot.api.swag.model.Queue;

import java.util.List;
import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException;

import java.io.InputStream;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.*;
import javax.validation.constraints.*;

@Path("/player")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the player API")

public class PlayerApi  {
   private final PlayerApiService delegate = PlayerApiServiceFactory.getPlayerApi();

    @DELETE
    @Path("/queue")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Removes a Song from the queue", notes = "Removes the specified Song from the current queue. If the queue did not contain the song, nothing is done.", response = Queue.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "The new queue", response = Queue.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "A parameter is missing", response = Queue.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The song could not be found", response = Queue.class) })
    public Response dequeue(@ApiParam(value = "the song's ID",required=true) @QueryParam("songId") String songId
,@ApiParam(value = "The ID of the provider the song is from",required=true) @QueryParam("providerId") String providerId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.dequeue(songId,providerId,securityContext);
    }
    @PUT
    @Path("/queue")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Adds a Song to the queue", notes = "Adds the specified Song to the current queue. If the queue already contains the Song, it won't be added.", response = Queue.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "The new queue", response = Queue.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "A parameter is missing", response = Queue.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The song could not be found", response = Queue.class) })
    public Response enqueue(@ApiParam(value = "The song's ID",required=true) @QueryParam("songId") String songId
,@ApiParam(value = "The ID of the provider the song is from",required=true) @QueryParam("providerId") String providerId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.enqueue(songId,providerId,securityContext);
    }
    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns the current player state", notes = "Returns the current player state. If the state is PLAY or PAUSE, it also contains the current song.", response = PlayerState.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "A PlayerState", response = PlayerState.class) })
    public Response getPlayerState(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getPlayerState(securityContext);
    }
    @GET
    @Path("/queue")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns the current player queue", notes = "", response = Queue.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "A list of Songs", response = Queue.class) })
    public Response getQueue(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getQueue(securityContext);
    }
    @PUT
    @Path("/pause")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Pauses the player", notes = "Pauses the current playback. If the current player state is not PLAY, does nothing.", response = PlayerState.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "A PlayerState", response = PlayerState.class) })
    public Response pausePlayer(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.pausePlayer(securityContext);
    }
    @PUT
    @Path("/play")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Resumes the player", notes = "Pauses the current playback. If the current player state is not PAUSE, does nothing.", response = PlayerState.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "A PlayerState", response = PlayerState.class) })
    public Response resumePlayer(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.resumePlayer(securityContext);
    }
}
