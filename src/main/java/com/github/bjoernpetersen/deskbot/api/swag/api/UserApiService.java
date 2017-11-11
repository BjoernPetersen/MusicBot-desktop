package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import com.github.bjoernpetersen.deskbot.api.swag.model.PasswordChange;
import com.github.bjoernpetersen.deskbot.api.swag.model.RegisterCredentials;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class UserApiService implements BotService {

  public abstract Response changePassword(String authorization, PasswordChange passwordChange,
      SecurityContext securityContext) throws NotFoundException;

  public abstract Response deleteUser(String authorization, SecurityContext securityContext)
      throws NotFoundException;

  public abstract Response registerUser(RegisterCredentials credentials,
      SecurityContext securityContext) throws NotFoundException;
}
