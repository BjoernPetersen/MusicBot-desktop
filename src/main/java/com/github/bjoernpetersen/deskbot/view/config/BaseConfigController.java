package com.github.bjoernpetersen.deskbot.view.config;

import com.github.bjoernpetersen.jmusicbot.config.Config;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
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
  @Nonnull
  private final Config.Entry[] additional;

  @FXML
  private StackPane configPane;

  public BaseConfigController(Config config, Config.Entry... additionalEntries) {
    this.config = config;
    this.additional = additionalEntries;
  }

  @Nonnull
  public Node createNode() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(PluginConfigController.class.getResource("BaseConfig.fxml"));
    loader.setController(this);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @FXML
  private void initialize() {
    List<Config.Entry> entries = new LinkedList<>(config.getDefaults().getEntries());
    entries.addAll(Arrays.asList(additional));
    configPane.getChildren().add(new ConfigController(
        config,
        FXCollections.observableList(entries)
    ).createConfigNode());
  }
}
