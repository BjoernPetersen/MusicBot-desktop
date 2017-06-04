package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.api.LookupApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException;
import com.github.bjoernpetersen.jmusicbot.provider.Provider;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

public class LookupApiServiceImpl extends LookupApiService {

  private ProviderManager manager;

  @Override
  public Response lookup(@NotNull String songId, @NotNull String providerId,
      SecurityContext securityContext) throws NotFoundException {
    if (manager == null) {
      return Response.serverError().entity("Not initialized").build();
    }

    Optional<Provider> providerOptional = Util.lookupProvider(manager, providerId);
    if (!providerOptional.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("Provider not found").build();
    }

    Provider provider = providerOptional.get();
    try {
      return Response.ok(Util.convert(provider.lookup(songId))).build();
    } catch (NoSuchSongException e) {
      return Response.status(Status.NOT_FOUND).entity("Song not found").build();
    }
  }

  @Override
  public void initialize(MusicBot bot) {
    manager = bot.getProviderManager();
  }
}
