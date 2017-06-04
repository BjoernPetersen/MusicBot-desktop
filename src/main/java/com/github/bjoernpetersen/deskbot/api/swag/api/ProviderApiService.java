package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import javax.validation.constraints.NotNull;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class ProviderApiService implements BotService {

  public abstract Response getProviders(SecurityContext securityContext) throws NotFoundException;

  public abstract Response lookupSong(String songId, String providerId,
      SecurityContext securityContext) throws NotFoundException;

  public abstract Response searchSong(String providerId, @NotNull String query,
      SecurityContext securityContext) throws NotFoundException;
}
