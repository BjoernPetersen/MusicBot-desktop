package com.github.bjoernpetersen.deskbot.api;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.github.bjoernpetersen.deskbot.api.swag.api.factories.PlayerApiServiceFactory;
import com.github.bjoernpetersen.deskbot.api.swag.api.factories.ProviderApiServiceFactory;
import com.github.bjoernpetersen.deskbot.api.swag.api.factories.SuggesterApiServiceFactory;
import com.github.bjoernpetersen.deskbot.api.swag.api.factories.UserApiServiceFactory;
import com.github.bjoernpetersen.jmusicbot.InitializationException;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

public class RestApi implements Closeable {

  private static final String BASE_URI_TEMPLATE = "http://0.0.0.0:%d/v1/";

  private final MusicBot bot;
  private final HttpServer server;

  public RestApi(MusicBot bot, int port) throws InitializationException {
    this.bot = bot;
    URI baseUri = URI.create(String.format(BASE_URI_TEMPLATE, port));
    server = GrizzlyHttpServerFactory.createHttpServer(
        baseUri,
        getResourceConfig(),
        false
    );

    initializeServices();

    try {
      server.start();
    } catch (IOException e) {
      throw new InitializationException(e);
    }
  }

  private void initializeServices() {
    PlayerApiServiceFactory.getPlayerApi().initialize(bot);
    ProviderApiServiceFactory.getProviderApi().initialize(bot);
    SuggesterApiServiceFactory.getSuggesterApi().initialize(bot);
    UserApiServiceFactory.getUserApi().initialize(bot);
  }

  @Override
  public void close() throws IOException {
    server.shutdownNow();
  }

  private ResourceConfig getResourceConfig() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.setSerializationInclusion(Include.NON_NULL);

    JacksonJaxbJsonProvider jacksonJaxbJsonProvider = new JacksonJaxbJsonProvider();
    jacksonJaxbJsonProvider.setMapper(objectMapper);

    return new ResourceConfig()
        .packages("com.github.bjoernpetersen.deskbot.api.swag.api")
        .register(jacksonJaxbJsonProvider);
  }
}
