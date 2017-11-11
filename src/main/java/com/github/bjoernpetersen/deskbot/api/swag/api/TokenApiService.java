package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import com.github.bjoernpetersen.deskbot.api.swag.model.LoginCredentials;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class TokenApiService implements BotService {

  public abstract Response login(LoginCredentials credentials, SecurityContext securityContext)
      throws NotFoundException;
}
