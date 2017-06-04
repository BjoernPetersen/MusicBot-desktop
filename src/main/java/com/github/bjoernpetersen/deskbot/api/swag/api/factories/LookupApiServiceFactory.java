package com.github.bjoernpetersen.deskbot.api.swag.api.factories;

import com.github.bjoernpetersen.deskbot.api.swag.api.LookupApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.impl.LookupApiServiceImpl;


public class LookupApiServiceFactory {

  private final static LookupApiService service = new LookupApiServiceImpl();

  public static LookupApiService getLookupApi() {
    return service;
  }
}
