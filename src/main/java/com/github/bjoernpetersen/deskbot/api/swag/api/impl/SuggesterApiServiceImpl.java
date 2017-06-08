package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException;
import com.github.bjoernpetersen.deskbot.api.swag.api.SuggesterApiService;
import com.github.bjoernpetersen.deskbot.api.swag.model.Song;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException;
import com.github.bjoernpetersen.jmusicbot.provider.Provider;
import com.github.bjoernpetersen.jmusicbot.provider.Suggester;
import com.github.bjoernpetersen.jmusicbot.user.InvalidTokenException;
import com.github.bjoernpetersen.jmusicbot.user.Permission;
import com.github.bjoernpetersen.jmusicbot.user.User;
import com.github.bjoernpetersen.jmusicbot.user.UserManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

public class SuggesterApiServiceImpl extends SuggesterApiService {

  private UserManager userManager;
  private ProviderManager providerManager;

  @Override
  public Response getSuggesters(SecurityContext securityContext) throws NotFoundException {
    return Response.ok(providerManager.getActiveSuggesters().keySet()).build();
  }

  @Override
  public Response suggestSong(String suggesterId, Integer max, SecurityContext securityContext)
      throws NotFoundException {
    Optional<Suggester> suggesterOptional = Util.lookupSuggester(providerManager, suggesterId);
    if (suggesterOptional.isPresent()) {
      Suggester suggester = suggesterOptional.get();
      int maxSuggestions = max == null || max < 1 || max > 64 ? 16 : max;
      List<Song> suggestions = suggester.getNextSuggestions(maxSuggestions).stream()
          .map(Util::convert)
          .collect(Collectors.toList());
      return Response.ok(suggestions).build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @Override
  public Response removeSuggestion(String suggesterId, String authorization, String songId,
      String providerId, SecurityContext securityContext) throws NotFoundException {
    User user;
    try {
      user = userManager.fromToken(authorization);
    } catch (InvalidTokenException e) {
      return Response.status(Status.UNAUTHORIZED).build();
    }

    if (!user.getPermissions().contains(Permission.DISLIKE)) {
      return Response.status(Status.FORBIDDEN).build();
    }

    Suggester suggester;
    com.github.bjoernpetersen.jmusicbot.Song song;
    try {
      Provider provider = providerManager.getProvider(providerId);
      suggester = providerManager.getSuggester(suggesterId);
      song = provider.lookup(songId);
    } catch (IllegalArgumentException | NoSuchSongException e) {
      return Response.status(Status.NOT_FOUND).build();
    }

    suggester.removeSuggestion(song);
    return Response.noContent().build();
  }

  @Override
  public void initialize(MusicBot bot) {
    userManager = bot.getUserManager();
    providerManager = bot.getProviderManager();
  }
}
