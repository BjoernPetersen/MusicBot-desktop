package com.github.bjoernpetersen.deskbot.api.swag.api.factories;

import com.github.bjoernpetersen.deskbot.api.swag.api.SearchApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.impl.SearchApiServiceImpl;


public class SearchApiServiceFactory {

  private final static SearchApiService service = new SearchApiServiceImpl();

  public static SearchApiService getSearchApi() {
    return service;
  }
}
