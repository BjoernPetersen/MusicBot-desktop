package com.github.bjoernpetersen.deskbot.api.swag.api.factories;

import com.github.bjoernpetersen.deskbot.api.swag.api.UserApiService;
import com.github.bjoernpetersen.deskbot.api.swag.api.impl.UserApiServiceImpl;


public class UserApiServiceFactory {

  private final static UserApiService service = new UserApiServiceImpl();

  public static UserApiService getUserApi() {
    return service;
  }
}
