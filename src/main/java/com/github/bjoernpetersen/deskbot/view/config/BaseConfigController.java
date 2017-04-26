package com.github.bjoernpetersen.deskbot.view.config;

import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.config.DefaultConfigEntry;
import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class BaseConfigController {

  @Nonnull
  private final Config config;

  @FXML
  private StackPane configPane;

  public BaseConfigController(Config config) {
    this.config = config;
  }

  @Nonnull
  public Node createNode() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(ProviderConfigController.class.getResource("BaseConfig.fxml"));
    loader.setController(this);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @FXML
  private void initialize() {
    configPane.getChildren().add(new ConfigController(
      config,
      FXCollections.observableList(DefaultConfigEntry.get(config).getEntries())
    ).createConfigNode());
  }
}
