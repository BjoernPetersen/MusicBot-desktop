package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.model.*;
import com.github.bjoernpetersen.deskbot.api.swag.api.SuggesterApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.factories.SuggesterApiServiceFactory;

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

@Path("/suggester")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the suggester API")

public class SuggesterApi  {
   private final SuggesterApiService delegate = SuggesterApiServiceFactory.getSuggesterApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns a list of all available suggesters", notes = "", response = String.class, responseContainer = "List", tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "A list of unique suggester IDs", response = String.class, responseContainer = "List") })
    public Response getSuggesters(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getSuggesters(securityContext);
    }
    @GET
    @Path("/{suggesterId}")
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns a list of suggestions", notes = "", response = Song.class, responseContainer = "List", tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "A list of suggested songs", response = Song.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Suggester not found", response = Song.class, responseContainer = "List") })
    public Response suggest(@ApiParam(value = "A suggester ID",required=true) @PathParam("suggesterId") String suggesterId
,@ApiParam(value = "The maximum size of the response. Defaults to 16.") @QueryParam("max") Integer max
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.suggest(suggesterId,max,securityContext);
    }
}
