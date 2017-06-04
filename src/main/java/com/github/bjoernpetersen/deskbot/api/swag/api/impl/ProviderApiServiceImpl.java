package com.github.bjoernpetersen.deskbot.api.swag.api.impl;

import com.github.bjoernpetersen.deskbot.api.swag.api.NotFoundException;
import com.github.bjoernpetersen.deskbot.api.swag.api.ProviderApiService;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ProviderApiServiceImpl extends ProviderApiService {

  private ProviderManager manager;

  @Override
  public Response getProviders(SecurityContext securityContext) throws NotFoundException {
    return Response.ok(manager.getActiveProviders().keySet()).build();
  }

  @Override
  public void initialize(MusicBot bot) {
    manager = bot.getProviderManager();
  }
}
