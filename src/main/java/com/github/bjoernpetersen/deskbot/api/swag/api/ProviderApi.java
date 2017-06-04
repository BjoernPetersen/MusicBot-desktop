package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.swag.model.*;
import com.github.bjoernpetersen.deskbot.api.swag.api.ProviderApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.factories.ProviderApiServiceFactory;

import io.swagger.annotations.ApiParam;
import io.swagger.jaxrs.*;


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

@Path("/provider")
@Consumes({ "application/json" })
@Produces({ "application/json" })
@io.swagger.annotations.Api(description = "the provider API")

public class ProviderApi  {
   private final ProviderApiService delegate = ProviderApiServiceFactory.getProviderApi();

    @GET
    
    @Consumes({ "application/json" })
    @Produces({ "application/json" })
    @io.swagger.annotations.ApiOperation(value = "Returns a list of all available providers", notes = "", response = String.class, responseContainer = "List", tags={  })
    @io.swagger.annotations.ApiResponses(value = { 
        @io.swagger.annotations.ApiResponse(code = 200, message = "A list of unique provider IDs", response = String.class, responseContainer = "List") })
    public Response getProviders(@Context SecurityContext securityContext)
    throws NotFoundException {
        return delegate.getProviders(securityContext);
    }
}
