package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.deskbot.model.BotHolder;
import com.github.bjoernpetersen.deskbot.model.ConfigStorage;
import com.github.bjoernpetersen.deskbot.model.PluginWrapper;
import com.github.bjoernpetersen.deskbot.view.config.BaseConfigController;
import com.github.bjoernpetersen.deskbot.view.config.ProviderConfigController;
import com.github.bjoernpetersen.jmusicbot.InitializationException;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.NamedPlugin;
import com.github.bjoernpetersen.jmusicbot.PlaybackFactoryManager;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.provider.Suggester;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
  private ListView<PluginWrapper<NamedPlugin>> pluginList;
  private ObservableList<PluginWrapper<NamedPlugin>> plugins;

  @FXML
  private Button startButton;

  private Stage stage;

  private Config config;
  private PlaybackFactoryManager playbackFactoryManager;
  private ProviderManager providerManager;
  private MusicBot.Builder builder;

  private Config.StringEntry defaultSuggester;

  public MainController() {
  }

  @FXML
  private void initialize() {
    plugins = FXCollections.observableArrayList();
    initializePluginList();

    config = new Config(
        new ConfigStorage(new File("config.properties"), new File("secrets.properties"))
    );
    defaultSuggester = config.stringEntry(
        getClass(), "defaultSuggester",
        "", null
    );

    playbackFactoryManager = loadPlaybackFactories();
    providerManager = loadProviderManager();
    addPlugins(providerManager.getProviders().values());
    addPlugins(providerManager.getSuggesters().values());
    builder = new MusicBot.Builder(config)
        .playbackFactoryManager(playbackFactoryManager)
        .providerManager(providerManager);

    startButton.setOnMouseClicked(this::start);

    pluginConfig.getChildren().add(new BaseConfigController(config).createNode());

    closeButton.managedProperty().bind(closeButton.visibleProperty());
    closeButton.setOnMouseClicked(event -> pluginList.getSelectionModel().select(null));
  }

  private PlaybackFactoryManager loadPlaybackFactories() {
    return new PlaybackFactoryManager(config, Collections.emptyList());
  }

  private ProviderManager loadProviderManager() {
    return new ProviderManager(config, playbackFactoryManager);
  }

  private void addPlugins(Collection<? extends NamedPlugin> plugins) {
    for (NamedPlugin plugin : plugins) {
      PluginWrapper<NamedPlugin> wrapper = new PluginWrapper<>(config, providerManager, plugin);
      this.plugins.add(wrapper);
    }
  }

  private void initializePluginList() {
    pluginList.setCellFactory(
        TextFieldListCell.forListView(new StringConverter<PluginWrapper<NamedPlugin>>() {
          @Override
          public String toString(PluginWrapper<NamedPlugin> wrapper) {
            return wrapper.getPlugin().getReadableName();
          }

          @Override
          public PluginWrapper<NamedPlugin> fromString(String string) {
            return null;
          }
        })
    );

    pluginList.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> {
          pluginConfig.getChildren().clear();
          if (newValue == null) {
            pluginName.setText("General");
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

    pluginList.setItems(this.plugins);
  }

  @Override
  public void showOnStage(@Nonnull Stage stage) {
    this.stage = stage;
    Scene scene = new Scene(root);
    stage.setScene(scene);
  }

  @Nullable
  private Suggester askForDefaultSuggesters() {
    Dialog<Suggester> dialog = new Dialog<>();
    DialogPane pane = new DialogPane();
    ChoiceBox<Suggester> choiceBox = new ChoiceBox<>();
    choiceBox.setConverter(new StringConverter<Suggester>() {
      @Override
      public String toString(Suggester object) {
        return object.getReadableName();
      }

      @Override
      public Suggester fromString(String string) {
        return null;
      }
    });
    choiceBox.setItems(FXCollections.observableList(getActiveSuggesters()));
    choiceBox.setValue(getDefaultSuggester(choiceBox.getItems()));
    pane.setHeaderText("Which suggester should be used if the queue is empty?");
    pane.setContent(choiceBox);
    pane.getButtonTypes().add(ButtonType.OK);
    pane.getButtonTypes().add(ButtonType.CANCEL);
    dialog.setDialogPane(pane);
    dialog.setResultConverter(param -> {
      if (param.equals(ButtonType.OK)) {
        return choiceBox.getValue();
      } else {
        return null;
      }
    });
    return dialog.showAndWait().orElse(null);
  }

  @Nonnull
  private List<Suggester> getActiveSuggesters() {
    List<Suggester> result = new LinkedList<>();
    for (PluginWrapper<NamedPlugin> wrapper : plugins) {
      NamedPlugin plugin = wrapper.getPlugin();
      if (plugin instanceof Suggester && wrapper.isActive()) {
        result.add((Suggester) plugin);
      }
    }
    return result;
  }


  @Nullable
  private Suggester getDefaultSuggester(List<Suggester> actives) {
    Optional<String> foundName = defaultSuggester.get();

    if (foundName.isPresent()) {
      String name = foundName.get();
      for (Suggester suggester : actives) {
        if (suggester.getName().equals(name)) {
          return suggester;
        }
      }
    }
    return null;
  }

  @FXML
  private void start(MouseEvent mouseEvent) {
    pluginList.getSelectionModel().select(null);
    startButton.setDisable(true);

    Suggester defaultSuggester = askForDefaultSuggesters();
    builder.defaultSuggester(defaultSuggester);
    this.defaultSuggester.set(defaultSuggester == null ? null : defaultSuggester.getName());

    new Thread(() -> {
      MusicBot bot;
      try {
        bot = builder.build();
      } catch (IllegalStateException e) {
        log.severe("Could not create MusicBot: " + e);
        e.printStackTrace();
        return;
      } catch (InitializationException e) {
        log.severe("Could not initialize MusicBot: " + e);
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
