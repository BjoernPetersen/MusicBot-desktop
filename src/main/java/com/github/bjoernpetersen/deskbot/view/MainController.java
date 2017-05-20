package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.deskbot.model.BotHolder;
import com.github.bjoernpetersen.deskbot.model.CompositeObservableList;
import com.github.bjoernpetersen.deskbot.model.ConfigStorage;
import com.github.bjoernpetersen.deskbot.model.PluginWrapper;
import com.github.bjoernpetersen.deskbot.view.config.BaseConfigController;
import com.github.bjoernpetersen.deskbot.view.config.ProviderConfigController;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.NamedPlugin;
import com.github.bjoernpetersen.jmusicbot.Plugin;
import com.github.bjoernpetersen.jmusicbot.PluginLoader;
import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.config.DefaultConfigEntry;
import com.github.bjoernpetersen.jmusicbot.provider.Provider;
import com.github.bjoernpetersen.jmusicbot.provider.Suggester;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javax.annotation.Nonnull;

public class MainController implements Window {

  private static final Logger log = Logger.getLogger(MainController.class.getName());

  @FXML
  private Parent root;

  @FXML
  private Pane centerPane;
  @FXML
  private Label pluginName;
  @FXML
  private Button closeButton;
  @FXML
  private StackPane pluginConfig;

  @FXML
  private ListView<PluginWrapper<?>> pluginList;

  @FXML
  private Button startButton;

  private Stage stage;
  private final Map<String, PluginWrapper<Provider>> providerNames;
  private final ObservableList<PluginWrapper<Provider>> providers;
  private final Map<String, PluginWrapper<Suggester>> suggesterNames;
  private final ObservableList<PluginWrapper<Suggester>> suggesters;


  private Config config;
  private MusicBot.Builder builder;

  public MainController() {
    providerNames = new HashMap<>();
    providers = FXCollections.observableArrayList();
    suggesterNames = new HashMap<>();
    suggesters = FXCollections.observableArrayList();
  }

  @FXML
  private void initialize() {
    initializePluginList();

    config = new Config(
        new ConfigStorage(new File("config.properties"), new File("secrets.properties"))
    );
    builder = new MusicBot.Builder(config);

    loadPlugins();
    // TODO dirty hack
    findDefaultSuggester();

    startButton.setOnMouseClicked(this::start);

    pluginConfig.getChildren().add(new BaseConfigController(config).createNode());

    closeButton.managedProperty().bind(closeButton.visibleProperty());
    closeButton.setOnMouseClicked(event -> pluginList.getSelectionModel().select(null));
  }

  private void findDefaultSuggester() {
    Config.StringEntry suggester = DefaultConfigEntry.get(config).suggester;
    if (!suggester.get().isPresent()) {
      for (PluginWrapper wrapper : providers) {
        if (wrapper.isActive() && wrapper.getPlugin() instanceof Suggester) {
          suggester.set(wrapper.getPlugin().getName());
        }
      }
    }
  }

  private void loadPlugins() {
    providerNames.clear();
    suggesterNames.clear();
    providers.clear();
    suggesters.clear();

    File pluginFolder = new File(DefaultConfigEntry.get(config).pluginFolder.getOrDefault());
    loadPlugins(pluginFolder, Provider.class, providerNames, providers);
    loadPlugins(pluginFolder, Suggester.class, suggesterNames, suggesters);
  }

  private <P extends NamedPlugin> void loadPlugins(File pluginFolder,
      Class<P> pluginClass,
      Map<String, PluginWrapper<P>> names,
      List<PluginWrapper<P>> plugins) {
    PluginLoader<P> loader = new PluginLoader<>(pluginFolder, pluginClass);
    for (P plugin : loader.load()) {
      PluginWrapper<P> wrapper = new PluginWrapper<>(config, plugin);
      names.put(plugin.getName(), wrapper);
      plugins.add(wrapper);
    }
  }

  private void initializePluginList() {
    pluginList.setCellFactory(
        TextFieldListCell.forListView(new StringConverter<PluginWrapper<?>>() {
          @Override
          public String toString(PluginWrapper<?> provider) {
            return provider.getPlugin().getReadableName();
          }

          @Override
          public PluginWrapper<?> fromString(String string) {
            return null;
          }
        })
    );

    pluginList.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> {
          pluginConfig.getChildren().clear();
          if (newValue == null) {
            pluginName.setText("General");
            findDefaultSuggester();
            pluginConfig.getChildren().add(new BaseConfigController(config).createNode());
          } else {
            pluginName.setText(newValue.getPlugin().getReadableName());
            pluginConfig.getChildren().add(
                new ProviderConfigController(config, newValue).createProviderConfig()
            );
          }
          closeButton.setVisible(newValue != null);
        }
    );

    pluginList.setItems(new CompositeObservableList<>(suggesters, providers));
  }

  @Override
  public void showOnStage(@Nonnull Stage stage) {
    this.stage = stage;
    Scene scene = new Scene(root);
    stage.setScene(scene);
  }

  @FXML
  private void start(MouseEvent mouseEvent) {
    pluginList.getSelectionModel().select(null);

    for (PluginWrapper<?> wrapper : pluginList.getItems()) {
      if (wrapper.isActive()) {
        Plugin plugin = wrapper.getPlugin();
        if (plugin instanceof Provider) {
          builder.addProvider((Provider) plugin);
        } else if (plugin instanceof Suggester) {
          builder.addSuggester((Suggester) plugin);
        } else {
          log.severe("Unknown plugin type: " + wrapper);
        }
      }
    }

    startButton.setDisable(true);
    findDefaultSuggester();
    new Thread(() -> {
      MusicBot bot;
      try {
        bot = builder.build();
      } catch (IllegalStateException e) {
        log.severe("Could not create MusicBot: " + e);
        e.printStackTrace();
        return;
      } finally {
        startButton.setDisable(false);
      }
      Platform.runLater(() -> {
        BotHolder.getInstance().set(bot);

        try {
          FXMLLoader loader = new FXMLLoader();
          loader.setLocation(PlayerController.class.getResource("Player.fxml"));
          loader.load();
          Window controller = loader.getController();
          controller.showOnStage(stage);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    }, "InitializationThread").start();
  }
}
