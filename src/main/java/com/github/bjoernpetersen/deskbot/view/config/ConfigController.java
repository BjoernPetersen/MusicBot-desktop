package com.github.bjoernpetersen.deskbot.view.config;

import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.config.Config.Entry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.logging.Logger;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ConfigController {

  @Nonnull
  private static final Logger log = Logger.getLogger(ConfigController.class.getName());

  @Nonnull
  private final Config config;
  @Nonnull
  private final ObservableList<? extends Config.Entry> entries;

  @FXML
  private GridPane grid;

  public ConfigController(Config config, ObservableList<? extends Config.Entry> entries) {
    this.config = config;
    this.entries = entries;
  }

  @Nonnull
  public Node createConfigNode() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(ProviderConfigController.class.getResource("Config.fxml"));
    loader.setController(this);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @FXML
  private void initialize() {
    entries.addListener((ListChangeListener<Entry>) c -> updateGrid(c.getList()));
    updateGrid(entries);
  }

  private void updateGrid(List<? extends Config.Entry> entries) {
    grid.getChildren().clear();

    for (int row = 0; row < entries.size(); row++) {
      Config.Entry entry = entries.get(row);

      String[] splitKey = entry.getKey().split("\\.");
      String shortKey = splitKey[splitKey.length - 1];
      grid.add(new Label(shortKey), 0, row);

      Label description = new Label(entry.getDescription());
      GridPane.setHgrow(description, Priority.ALWAYS);
      Tooltip.install(description, new Tooltip(description.getText()));
      grid.add(description, 1, row);

      grid.add(createEditable(entry), 2, row);
    }
  }

  @Nonnull
  private Node createEditable(Config.Entry entry) {
    if (entry instanceof Config.StringEntry) {
      Config.StringEntry stringEntry = (Config.StringEntry) entry;
      if (stringEntry.isSecret()) {
        return createSecretTextField(stringEntry);
      } else {
        return createPlainTextField(stringEntry);
      }
    } else {
      Config.BooleanEntry booleanEntry = (Config.BooleanEntry) entry;
      return createCheckbox(booleanEntry);
    }
  }

  @Nonnull
  private Node createPlainTextField(Config.StringEntry entry) {
    TextField field = new TextField(entry.get().orElse(null));
    field.setPromptText(entry.getDefault().orElse(null));
    field.textProperty().addListener(((observable, oldValue, newValue) -> {
      entry.set(newValue);
    }));
    return field;
  }

  @Nonnull
  private Node createSecretTextField(Config.StringEntry entry) {
    PasswordField field = new PasswordField();
    field.setText(entry.getWithDefault().orElse(null));
    field.textProperty().addListener(((observable, oldValue, newValue) -> {
      entry.set(newValue);
    }));
    return field;
  }

  @Nonnull
  private Node createCheckbox(Config.BooleanEntry entry) {
    CheckBox checkBox = new CheckBox();
    checkBox.setSelected(entry.get());
    checkBox.selectedProperty().addListener(((observable, oldValue, newValue) -> {
      entry.set(newValue);
    }));
    return checkBox;
  }
}
