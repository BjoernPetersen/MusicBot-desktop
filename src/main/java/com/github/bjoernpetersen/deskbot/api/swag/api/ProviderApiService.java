package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class ProviderApiService implements BotService {

  public abstract Response getProviders(SecurityContext securityContext) throws NotFoundException;
}
