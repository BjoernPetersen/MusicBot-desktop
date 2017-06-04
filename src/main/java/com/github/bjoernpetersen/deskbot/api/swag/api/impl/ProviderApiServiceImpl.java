package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException;
import com.github.bjoernpetersen.deskbot.api.swag.api.ProviderApiService;
import com.github.bjoernpetersen.deskbot.api.swag.model.Song;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException;
import com.github.bjoernpetersen.jmusicbot.provider.Provider;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

public class ProviderApiServiceImpl extends ProviderApiService {

  private ProviderManager manager;

  @Override
  public Response getProviders(SecurityContext securityContext) throws NotFoundException {
    return Response.ok(manager.getActiveProviders().keySet()).build();
  }

  @Override
  public Response lookupSong(String songId, String providerId, SecurityContext securityContext)
      throws NotFoundException {
    if (manager == null) {
      return Response.serverError().entity("Not initialized").build();
    }

    Optional<Provider> providerOptional = Util.lookupProvider(manager, providerId);
    if (!providerOptional.isPresent()) {
      return Response.status(Status.NOT_FOUND).build();
    }

    Provider provider = providerOptional.get();
    try {
      return Response.ok(Util.convert(provider.lookup(songId))).build();
    } catch (NoSuchSongException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @Override
  public Response searchSong(String providerId, @NotNull String query,
      SecurityContext securityContext) throws NotFoundException {
    if (manager == null) {
      return Response.serverError().entity("Not initialized").build();
    }
    Optional<Provider> providerOptional = Util.lookupProvider(manager, providerId);
    if (!providerOptional.isPresent()) {
      return Response.status(Status.NOT_FOUND).build();
    }

    Provider provider = providerOptional.get();
    List<Song> searchResult = provider.search(query).stream()
        .map(Util::convert)
        .collect(Collectors.toList());
    return Response.ok(searchResult).build();
  }

  @Override
  public void initialize(MusicBot bot) {
    manager = bot.getProviderManager();
  }
}
