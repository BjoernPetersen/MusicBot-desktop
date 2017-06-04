package com.github.bjoernpetersen.deskbot.api.swag.api.factories;

import com.github.bjoernpetersen.deskbot.api.swag.api.PlayerApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.impl.PlayerApiServiceImpl;


public class PlayerApiServiceFactory {

  private final static PlayerApiService service = new PlayerApiServiceImpl();

  public static PlayerApiService getPlayerApi() {
    return service;
  }
}
