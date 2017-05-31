package com.github.bjoernpetersen.deskbot.view.config;

import com.github.bjoernpetersen.jmusicbot.config.Config;
import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.beans.property.BooleanProperty;
import javafx.collections.ObservableList;
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
  private final BooleanProperty activeProperty;
  @Nonnull
  private final ObservableList<? extends Config.Entry> configEntries;

  public ProviderConfigController(Config config, BooleanProperty activeProperty,
      ObservableList<? extends Config.Entry> configEntries) {
    this.config = config;
    this.activeProperty = activeProperty;
    this.configEntries = configEntries;
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
        new ConfigController(config, configEntries).createConfigNode()
    );
    configPane.visibleProperty().bind(activeProperty);
    activateCheckbox.selectedProperty().set(activeProperty.get());
    activateCheckbox.selectedProperty().bindBidirectional(activeProperty);
  }

}
