package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class SuggesterApiService implements BotService {

  public abstract Response getSuggesters(SecurityContext securityContext) throws NotFoundException;

  public abstract Response removeSuggestion(String suggesterId, String authorization,
      @NotNull String songId, @NotNull String providerId, SecurityContext securityContext)
      throws NotFoundException;

  public abstract Response suggestSong(String suggesterId, @Min(1) @Max(64) Integer max,
      SecurityContext securityContext) throws NotFoundException;
}
