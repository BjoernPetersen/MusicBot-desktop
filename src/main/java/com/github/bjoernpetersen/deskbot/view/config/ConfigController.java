package com.github.bjoernpetersen.deskbot.view.config;

import static com.github.bjoernpetersen.deskbot.view.config.ConfigNodeFactoryKt.createNode;

import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.config.Config.Entry;
import com.github.bjoernpetersen.jmusicbot.config.Config.ReadOnlyStringEntry;
import com.github.bjoernpetersen.jmusicbot.config.ConfigListener;
import com.github.bjoernpetersen.jmusicbot.config.WeakConfigListener;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
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
  @Nonnull
  private final List<ConfigListener<String>> validityListeners;

  @FXML
  private GridPane grid;

  public ConfigController(Config config, ObservableList<? extends Config.Entry> entries) {
    this.config = config;
    this.entries = entries;
    this.validityListeners = new LinkedList<>();
  }

  @Nonnull
  public Node createConfigNode() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(PluginConfigController.class.getResource("Config.fxml"));
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
    validityListeners.clear();
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

      grid.add(createNode(() -> grid.getScene().getWindow(), entry), 2, row);

      if (entry instanceof Config.ReadOnlyStringEntry) {
        // check for validity on change
        Node warningNode = new Label();
        warningNode.getStyleClass().addAll("alert", "warning", "dialog-pane");
        grid.add(warningNode, 3, row);
        Tooltip tooltip = new Tooltip();
        Tooltip.install(warningNode, tooltip);

        Config.ReadOnlyStringEntry stringEntry = (ReadOnlyStringEntry) entry;
        ConfigListener<String> listener = (o, n) -> {
          String warning = stringEntry.checkError();
          if (warning != null) {
            tooltip.setText(warning);
          }
          warningNode.setVisible(warning != null);
        };
        validityListeners.add(listener);
        stringEntry.addListener(new WeakConfigListener<>(listener));
        listener.onChange(null, stringEntry.getValue());
      }
    }
  }
}
