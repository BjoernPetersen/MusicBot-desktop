package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException;
import com.github.bjoernpetersen.deskbot.api.swag.api.SuggesterApiService;
import com.github.bjoernpetersen.deskbot.api.swag.model.Song;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.provider.Suggester;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

public class SuggesterApiServiceImpl extends SuggesterApiService {

  private ProviderManager manager;

  @Override
  public Response getSuggesters(SecurityContext securityContext) throws NotFoundException {
    return Response.ok(manager.getActiveSuggesters().keySet()).build();
  }

  @Override
  public Response suggestSong(String suggesterId, Integer max, SecurityContext securityContext)
      throws NotFoundException {
    Optional<Suggester> suggesterOptional = Util.lookupSuggester(manager, suggesterId);
    if (suggesterOptional.isPresent()) {
      Suggester suggester = suggesterOptional.get();
      List<Song> suggestions = suggester.getNextSuggestions(max).stream()
          .map(Util::convert)
          .collect(Collectors.toList());
      return Response.ok(suggestions).build();
    } else {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @Override
  public void initialize(MusicBot bot) {
    manager = bot.getProviderManager();
  }
}
