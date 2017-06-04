package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class SearchApiService implements BotService {

  public abstract Response search(@NotNull String providerId, @NotNull String query,
      SecurityContext securityContext) throws NotFoundException;
}
