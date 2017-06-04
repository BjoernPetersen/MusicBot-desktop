package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.model.*;
import com.github.bjoernpetersen.deskbot.api.swag.api.SearchApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.factories.SearchApiServiceFactory;

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

@Path("/search")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the search API")

public class SearchApi  {
   private final SearchApiService delegate = SearchApiServiceFactory.getSearchApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Searches for songs", notes = "", response = Song.class, responseContainer = "List", tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "A list of results", response = Song.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 400, message = "A parameter is missing", response = Song.class, responseContainer = "List"),
        
        @io.swagger.annotations.ApiResponse(code = 404, message = "Invalid provider ID", response = Song.class, responseContainer = "List") })
    public Response search(@ApiParam(value = "The ID of the provider to search with",required=true) @QueryParam("providerId") String providerId
,@ApiParam(value = "A search query",required=true) @QueryParam("query") String query
,@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.search(providerId,query,securityContext);
    }
}
