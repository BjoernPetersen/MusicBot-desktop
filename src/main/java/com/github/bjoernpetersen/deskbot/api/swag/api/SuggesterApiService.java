package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class SuggesterApiService implements BotService {

  public abstract Response getSuggesters(SecurityContext securityContext) throws NotFoundException;

  public abstract Response suggestSong(String suggesterId, Integer max,
      SecurityContext securityContext) throws NotFoundException;
}
