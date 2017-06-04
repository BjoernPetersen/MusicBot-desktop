package com.github.bjoernpetersen.deskbot.api.swag.api.factories;

import com.github.bjoernpetersen.deskbot.api.swag.api.ProviderApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.impl.ProviderApiServiceImpl;


public class ProviderApiServiceFactory {

  private final static ProviderApiService service = new ProviderApiServiceImpl();

  public static ProviderApiService getProviderApi() {
    return service;
  }
}
