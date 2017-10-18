package com.github.bjoernpetersen.deskbot.view.config;

import com.github.bjoernpetersen.jmusicbot.config.Config;
import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.StackPane;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class PluginConfigController {

  @FXML
  private CheckBox activateCheckbox;
  @FXML
  private StackPane configPane;

  @Nonnull
  private final Config config;
  @Nonnull
  private final BooleanProperty activeProperty;
  @Nonnull
  private final ObservableList<? extends Config.Entry> configEntries;
  private final boolean isDeactivatable;

  public PluginConfigController(Config config,
      ObservableList<? extends Config.Entry> configEntries) {
    this(config, new SimpleBooleanProperty(true), configEntries, false);
  }

  public PluginConfigController(Config config, BooleanProperty activeProperty,
      ObservableList<? extends Config.Entry> configEntries) {
    this(config, activeProperty, configEntries, true);
  }

  public PluginConfigController(Config config, BooleanProperty activeProperty,
      ObservableList<? extends Config.Entry> configEntries, boolean isDeactivatable) {
    this.config = config;
    this.activeProperty = activeProperty;
    this.configEntries = configEntries;
    this.isDeactivatable = isDeactivatable;
  }

  @Nonnull
  public Node createProviderConfig() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(PluginConfigController.class.getResource("ProviderConfig.fxml"));
    loader.setController(this);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @FXML
  private void initialize() {
    activateCheckbox.setVisible(isDeactivatable);
    activateCheckbox.setManaged(isDeactivatable);
    configPane.getChildren().add(
        new ConfigController(config, configEntries).createConfigNode()
    );
    configPane.visibleProperty().bind(activeProperty);
    activateCheckbox.selectedProperty().set(activeProperty.get());
    activateCheckbox.selectedProperty().bindBidirectional(activeProperty);
  }

}
