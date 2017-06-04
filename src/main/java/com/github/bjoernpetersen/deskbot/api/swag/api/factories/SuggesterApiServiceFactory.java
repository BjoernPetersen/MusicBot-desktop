package com.github.bjoernpetersen.deskbot.api.swag.api.factories;

import com.github.bjoernpetersen.deskbot.api.swag.api.SuggesterApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.impl.SuggesterApiServiceImpl;


public class SuggesterApiServiceFactory {

  private final static SuggesterApiService service = new SuggesterApiServiceImpl();

  public static SuggesterApiService getSuggesterApi() {
    return service;
  }
}
