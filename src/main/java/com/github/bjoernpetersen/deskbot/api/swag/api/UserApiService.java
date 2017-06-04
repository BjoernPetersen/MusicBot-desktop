package com.github.bjoernpetersen.deskbot.api.swag.api;

import com.github.bjoernpetersen.deskbot.api.BotService;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class UserApiService implements BotService {

  public abstract Response deleteUser(String authorization, SecurityContext securityContext)
      throws NotFoundException;

  public abstract Response login(String userName, String password, String uuid,
      SecurityContext securityContext)
      throws NotFoundException;

  public abstract Response registerUser(String userName, String uuid,
      SecurityContext securityContext)
      throws NotFoundException;

  public abstract Response changePassword(String authorization, String password, String oldPassword,
      SecurityContext securityContext);
}
