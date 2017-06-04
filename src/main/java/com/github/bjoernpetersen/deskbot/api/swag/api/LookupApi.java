package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.model.*;
import com.github.bjoernpetersen.deskbot.api.swag.api.LookupApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.factories.LookupApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;

import com.github.bjoernpetersen.deskbot.api.swag.model.Song;

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

@Path("/lookup")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the lookup API")

public class LookupApi  {
   private final LookupApiService delegate = LookupApiServiceFactory.getLookupApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Looks up a song", notes = "Looks up a song using its ID and a provider ID", response = Song.class, tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "The looked up song", response = Song.class),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "A parameter is missing", response = Song.class),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "The song could not be found", response = Song.class) })
    public Response lookup(@ApiParam(value = "A song ID",required=true) @QueryParam("songId") String songId
,@ApiParam(value = "A provider ID",required=true) @QueryParam("providerId") String providerId
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.lookup(songId,providerId,securityContext);
    }
}
