package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.deskbot.JavafxHostServices;
import com.github.bjoernpetersen.deskbot.api.Broadcaster;
import com.github.bjoernpetersen.deskbot.api.RestApi;
import com.github.bjoernpetersen.deskbot.model.ConfigStorage;
import com.github.bjoernpetersen.deskbot.model.ObservableProviderWrapper;
import com.github.bjoernpetersen.deskbot.model.ObservableSuggesterWrapper;
import com.github.bjoernpetersen.deskbot.model.PlaybackFactoryWrapper;
import com.github.bjoernpetersen.deskbot.view.config.BaseConfigController;
import com.github.bjoernpetersen.deskbot.view.config.PluginConfigController;
import com.github.bjoernpetersen.jmusicbot.IdPlugin;
import com.github.bjoernpetersen.jmusicbot.Loggable;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.PlaybackFactoryManager;
import com.github.bjoernpetersen.jmusicbot.Plugin;
import com.github.bjoernpetersen.jmusicbot.Plugin.State;
import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.platform.Platform;
import com.github.bjoernpetersen.jmusicbot.provider.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.provider.ProviderManager.ProviderWrapper;
import com.github.bjoernpetersen.jmusicbot.provider.ProviderManager.SuggesterWrapper;
import com.github.bjoernpetersen.jmusicbot.provider.Suggester;
import com.github.bjoernpetersen.jmusicbot.user.UserManager;
import io.sentry.Sentry;
import io.sentry.event.User;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MainController implements Loggable, Window {

  @FXML
  private Parent root;

  @FXML
  private Pane centerPane;
  @FXML
  private Label pluginName;
  @FXML
  private Label pluginType;
  @FXML
  private Label pluginPlatforms;
  @FXML
  private Button closeButton;
  @FXML
  private StackPane pluginConfig;

  @FXML
  private TabPane tabPane;
  @FXML
  private ListView<PlaybackFactoryWrapper> playbackFactoryList;
  @FXML
  private ListView<ObservableProviderWrapper> providerList;
  @FXML
  private ListView<ObservableSuggesterWrapper> suggesterList;

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
    config = new Config(
        new ConfigStorage(new File("config.properties"), new File("secrets.properties")),
        new JavafxHostServices()
    );

    ProviderWrapper.setDefaultFactory(p -> new ObservableProviderWrapper(config, p));
    SuggesterWrapper.setDefaultFactory(s -> new ObservableSuggesterWrapper(config, s));
    initializePluginLists();

    configureSentryUser(config);
    defaultSuggester = config.new StringEntry(
        getClass(), "defaultSuggester",
        "", false, null
    );

    playbackFactoryManager = new PlaybackFactoryManager(config, Collections.emptyList());
    providerManager = ProviderManager.defaultManager();
    try {
      providerManager.initialize(config, playbackFactoryManager);
    } catch (AbstractMethodError e) {
      logInfo(e, "Some plugin is outdated");
      Alert alert = new Alert(AlertType.ERROR);
      alert.setResizable(true);
      alert.setHeaderText("Outdated plugin");
      StringWriter writer = new StringWriter();
      e.printStackTrace(new PrintWriter(writer));
      alert.setContentText(writer.toString());
      alert.showAndWait();
      System.exit(0);
    }
    fillPluginLists();
    builder = new MusicBot.Builder(config)
        .playbackFactoryManager(playbackFactoryManager)
        .providerManager(providerManager);

    startButton.setOnMouseClicked(this::start);

    pluginConfig.getChildren().add(new BaseConfigController(config).createNode());

    closeButton.managedProperty().bind(closeButton.visibleProperty());
    closeButton.setOnMouseClicked(event -> selectNull());
    tabPane.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> selectNull()
    );
  }

  private void configureSentryUser(Config config) {
    Config.StringEntry userIdEntry = config.new StringEntry(getClass(), "sentryUser", "", true);
    String storedId = userIdEntry.getValue();
    String userId;
    if (storedId == null) {
      userIdEntry.set(userId = UUID.randomUUID().toString());
    } else {
      userId = storedId;
    }
    logFine("Sentry user ID: " + userId);
    User user = new User(userId, null, null, null);
    Sentry.setUser(user);
  }

  private void selectNull() {
    playbackFactoryList.getSelectionModel().select(null);
    providerList.getSelectionModel().select(null);
    suggesterList.getSelectionModel().select(null);
  }

  private void fillPluginLists() {
    playbackFactoryList.getItems().addAll(playbackFactoryManager.getPlaybackFactories().stream()
        .map(PlaybackFactoryWrapper::new)
        .collect(Collectors.toList())
    );
    providerList.getItems().addAll(
        (Collection<ObservableProviderWrapper>) providerManager.getAllProviders().values()
    );
    suggesterList.getItems().addAll(
        (Collection<ObservableSuggesterWrapper>) providerManager.getAllSuggesters().values()
    );
  }

  private <T extends IdPlugin> StringConverter<T> createStringConverter() {
    return new StringConverter<T>() {
      @Override
      public String toString(T object) {
        return object.getReadableName();
      }

      @Override
      public T fromString(String string) {
        return null;
      }
    };
  }

  private <T extends IdPlugin> ChangeListener<T> createChangeListener(String pluginType,
      Function<T, PluginConfigController> controllerFunction) {
    return (observable, oldValue, newValue) -> {
      pluginConfig.getChildren().clear();
      if (newValue == null) {
        pluginName.setText("General");
        pluginPlatforms.setText("");
        this.pluginType.setText("");
        pluginConfig.getChildren().add(new BaseConfigController(config).createNode());
      } else {
        pluginName.setText(newValue.getReadableName());
        pluginPlatforms.setText(getPlatformString(newValue));
        this.pluginType.setText(pluginType);
        pluginConfig.getChildren().add(controllerFunction.apply(newValue).createProviderConfig());
      }
      closeButton.setVisible(newValue != null);
    };
  }

  private String getPlatformString(Plugin plugin) {
    StringJoiner joiner = new StringJoiner(", ").setEmptyValue("None");
    getPlatformString(plugin, Platform.WINDOWS).ifPresent(joiner::add);
    getPlatformString(plugin, Platform.LINUX).ifPresent(joiner::add);
    getPlatformString(plugin, Platform.ANDROID).ifPresent(joiner::add);
    return joiner.toString();
  }

  @Nonnull
  private Optional<String> getPlatformString(Plugin plugin, Platform platform) {
    switch (plugin.getSupport(platform)) {
      case YES:
        return Optional.of(platform.getReadableName());
      case MAYBE:
        return Optional.of(platform.getReadableName() + '?');
      case NO:
      default:
        return Optional.empty();
    }
  }

  private void initializePluginLists() {
    initializePluginList(playbackFactoryList, createChangeListener(
        "PlaybackFactory",
        f -> new PluginConfigController(
            config,
            FXCollections.observableList(playbackFactoryManager.getConfigEntries(f.getWrapped())))
        )
    );

    initializePluginList(providerList, createChangeListener(
        "Provider",
        p -> {
          return new PluginConfigController(
              config,
              p.activeProperty(),
              p.getObservableConfigEntries()
          );
        }
    ));
    initializePluginList(suggesterList, createChangeListener("Suggester",
        s -> {
          return new PluginConfigController(
              config,
              s.activeProperty(),
              s.getObservableConfigEntries()
          );
        }
    ));
  }

  private <T extends IdPlugin> void initializePluginList(ListView<T> list,
      ChangeListener<T> selectListener) {
    list.setCellFactory(TextFieldListCell.forListView(createStringConverter()));
    list.getSelectionModel().selectedItemProperty().addListener(selectListener);
  }

  @Override
  public void showOnStage(@Nonnull Stage stage) {
    this.stage = stage;
    Scene scene = new Scene(root);
    stage.setScene(scene);
  }

  @Nullable
  private Suggester askForDefaultSuggesters(boolean noConfig) {
    if (noConfig) {
      return getDefaultSuggester(getActiveSuggesters());
    }
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
    return suggesterList.getItems().stream()
        .filter(s -> s.getState() == State.CONFIG)
        .collect(Collectors.toList());
  }

  @Nullable
  private Suggester getDefaultSuggester(List<Suggester> actives) {
    String name = defaultSuggester.getValue();

    if (name != null) {
      for (Suggester suggester : actives) {
        if (suggester.getId().equals(name)) {
          return suggester;
        }
      }
    }
    return null;
  }

  public void start(boolean noConfig) {
    selectNull();

    Suggester defaultSuggester = askForDefaultSuggesters(noConfig);
    builder.defaultSuggester(defaultSuggester);
    this.defaultSuggester.set(defaultSuggester == null ? null : defaultSuggester.getId());

    try {
      builder.userManager(new UserManager(config, "jdbc:sqlite:users.db"));
    } catch (SQLException e) {
      logSevere(e, "Could not connect to database: ");
      return;
    }
    builder.apiInitializer(RestApi::new);
    builder.broadcasterInitializer(Broadcaster::new);

    PluginLoaderController.load(stage, builder);
  }

  @FXML
  private void start(MouseEvent mouseEvent) {
    start(false);
  }
}
