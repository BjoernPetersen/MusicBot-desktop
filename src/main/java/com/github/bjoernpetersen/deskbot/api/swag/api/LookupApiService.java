package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class LookupApiService implements BotService {

  public abstract Response lookup(@NotNull String songId, @NotNull String providerId,
      SecurityContext securityContext) throws NotFoundException;
}
