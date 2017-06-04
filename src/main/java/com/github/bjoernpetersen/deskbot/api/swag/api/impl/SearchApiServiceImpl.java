package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException;
import com.github.bjoernpetersen.deskbot.api.swag.api.SearchApiService;
import com.github.bjoernpetersen.deskbot.api.swag.model.Song;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.provider.Provider;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

public class SearchApiServiceImpl extends SearchApiService {

  private ProviderManager providerManager;

  @Override
  public Response search(@NotNull String providerId, @NotNull String query,
      SecurityContext securityContext) throws NotFoundException {
    if (providerManager == null) {
      return Response.serverError().entity("Not initialized").build();
    }
    Optional<Provider> providerOptional = Util.lookupProvider(providerManager, providerId);
    if (!providerOptional.isPresent()) {
      return Response.status(Status.NOT_FOUND).entity("Provider not found").build();
    }

    Provider provider = providerOptional.get();
    List<Song> searchResult = provider.search(query).stream()
        .map(Util::convert)
        .collect(Collectors.toList());
    return Response.ok(searchResult).

        build();

  }

  @Override
  public void initialize(MusicBot bot) {
    providerManager = bot.getProviderManager();
  }
}
