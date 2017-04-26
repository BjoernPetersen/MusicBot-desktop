package com.github.bjoernpetersen.deskbot.view.config;

import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.deskbot.model.PluginWrapper;
import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ProviderConfigController {

  @FXML
  private CheckBox activateCheckbox;
  @FXML
  private StackPane configPane;

  @Nonnull
  private final Config config;
  @Nonnull
  private PluginWrapper pluginWrapper;

  public ProviderConfigController(Config config, PluginWrapper pluginWrapper) {
    this.config = config;
    this.pluginWrapper = pluginWrapper;
  }

  @Nonnull
  public Node createProviderConfig() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(ProviderConfigController.class.getResource("ProviderConfig.fxml"));
    loader.setController(this);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @FXML
  private void initialize() {
    configPane.getChildren().add(
      new ConfigController(config, pluginWrapper.getConfigEntries()).createConfigNode()
    );
    configPane.visibleProperty().bind(pluginWrapper.activeProperty());
    activateCheckbox.selectedProperty().set(pluginWrapper.isActive());
    activateCheckbox.selectedProperty().bindBidirectional(pluginWrapper.activeProperty());
  }

}
