package com.github.bjoernpetersen.deskbot.api.swag.api.factories;

import com.github.bjoernpetersen.deskbot.api.swag.api.TokenApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.impl.TokenApiServiceImpl;


public class TokenApiServiceFactory {

  private final static TokenApiService service = new TokenApiServiceImpl();

  public static TokenApiService getTokenApi() {
    return service;
  }
}
