package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.deskbot.JavafxHostServices;
import com.github.bjoernpetersen.deskbot.api.Broadcaster;
import com.github.bjoernpetersen.deskbot.api.RestApi;
import com.github.bjoernpetersen.deskbot.model.ConfigStorage;
import com.github.bjoernpetersen.deskbot.model.ObservableAdminWrapper;
import com.github.bjoernpetersen.deskbot.model.ObservableProviderWrapper;
import com.github.bjoernpetersen.deskbot.model.ObservableSuggesterWrapper;
import com.github.bjoernpetersen.deskbot.model.PlaybackFactoryWrapper;
import com.github.bjoernpetersen.deskbot.model.SuggesterChoice;
import com.github.bjoernpetersen.deskbot.view.config.BaseConfigController;
import com.github.bjoernpetersen.deskbot.view.config.ConfigController;
import com.github.bjoernpetersen.deskbot.view.config.PluginConfigController;
import com.github.bjoernpetersen.jmusicbot.AdminPlugin;
import com.github.bjoernpetersen.jmusicbot.Configurator.Result;
import com.github.bjoernpetersen.jmusicbot.Loggable;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.PlaybackFactoryManager;
import com.github.bjoernpetersen.jmusicbot.Plugin;
import com.github.bjoernpetersen.jmusicbot.Plugin.State;
import com.github.bjoernpetersen.jmusicbot.PluginLoader;
import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.config.ui.ChoiceBox;
import com.github.bjoernpetersen.jmusicbot.config.ui.DefaultStringConverter;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
  private ListView<ObservableAdminWrapper> adminList;

  @FXML
  private Button startButton;

  private Stage stage;

  private Config config;
  private PlaybackFactoryManager playbackFactoryManager;
  private ProviderManager providerManager;
  private List<ObservableAdminWrapper> adminPlugins;
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
        getClass(),
        "defaultSuggester",
        "",
        false,
        null,
        new ChoiceBox<>(
            () -> getActiveSuggesters().stream()
                .map(SuggesterChoice::new)
                .collect(Collectors.toList()),
            DefaultStringConverter.INSTANCE,
            false
        )
    );

    playbackFactoryManager = new PlaybackFactoryManager(config, Collections.emptyList());
    providerManager = ProviderManager.defaultManager();
    try {
      providerManager.initialize(config, playbackFactoryManager);
    } catch (AbstractMethodError e) {
      showOutdated(e);
      return;
    }
    loadAdminPlugins();
    fillPluginLists();
    builder = new MusicBot.Builder(config)
        .configurator(this::askForMissingConfig)
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

  private void loadAdminPlugins() {
    String pluginFolderName = config.getDefaults().getPluginFolder().getValue();
    File pluginFolder = new File(pluginFolderName);
    PluginLoader<AdminPlugin> loader = new PluginLoader<>(pluginFolder, AdminPlugin.class);

    Collection<AdminPlugin> plugins;
    try {
      plugins = loader.load();
    } catch (AbstractMethodError e) {
      showOutdated(e);
      return;
    }
    adminPlugins = new ArrayList<>(plugins.size());
    for (AdminPlugin plugin : plugins) {
      adminPlugins.add(new ObservableAdminWrapper(config, plugin));
    }
  }

  private void showOutdated(AbstractMethodError e) {
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
    adminList.getSelectionModel().select(null);
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
    adminList.getItems().addAll(adminPlugins);
  }

  private <T extends Plugin> StringConverter<T> createStringConverter() {
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

  private <T extends Plugin> ChangeListener<T> createChangeListener(String pluginType,
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
    initializePluginList(adminList, createChangeListener("AdminPlugin",
        a -> {
          return new PluginConfigController(
              config,
              a.activeProperty(),
              a.getObservableConfigEntries()
          );
        }
    ));
  }

  private <T extends Plugin> void initializePluginList(ListView<T> list,
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

  private Result askForMissingConfig(String plugin,
      List<? extends Config.Entry> entries) {
    Lock lock = new ReentrantLock();
    Condition done = lock.newCondition();
    Result[] result = new Result[1];
    Runnable ask = () -> {
      Dialog<Result> dialog = new Dialog<>();
      dialog.setTitle("Missing config entries");
      DialogPane pane = new DialogPane();
      pane.setHeaderText("Missing config entries for " + plugin);
      pane.setContent(
          new ConfigController(config, FXCollections.observableList(entries)).createConfigNode()
      );
      pane.getButtonTypes().add(ButtonType.OK);
      pane.getButtonTypes().add(ButtonType.NO);
      ((Button) pane.lookupButton(ButtonType.NO)).setText("Disable");
      pane.getButtonTypes().add(ButtonType.CANCEL);
      dialog.setDialogPane(pane);
      dialog.setResultConverter(bt -> {
        if (bt.equals(ButtonType.OK)) {
          return Result.OK;
        } else if (bt.equals(ButtonType.NO)) {
          return Result.DISABLE;
        } else if (bt.equals(ButtonType.CANCEL)) {
          return Result.CANCEL;
        } else {
          logWarning("No button was pressed somehow");
          return Result.CANCEL;
        }
      });
      result[0] = dialog.showAndWait().orElseThrow(IllegalStateException::new);
      lock.lock();
      try {
        done.signalAll();
      } finally {
        lock.unlock();
      }
    };

    if (javafx.application.Platform.isFxApplicationThread()) {
      ask.run();
    } else {
      javafx.application.Platform.runLater(ask);
      lock.lock();
      try {
        done.awaitUninterruptibly();
      } finally {
        lock.unlock();
      }
    }

    return result[0];
  }

  /**
   * Gets all suggesters in CONFIG state.
   *
   * @return a list of suggesters
   */
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

    if (!noConfig || getDefaultSuggester(getActiveSuggesters()) == null) {
      Result result = askForMissingConfig(
          "base functionality",
          Collections.singletonList(defaultSuggester)
      );
      switch (result) {
        case DISABLE:
          defaultSuggester.set(null);
          break;
        case CANCEL:
          logFine("User cancelled config");
          return;
        case OK:
        default:
          // nothing to see here
      }
    }

    Suggester defaultSuggester = getDefaultSuggester(getActiveSuggesters());
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
    adminPlugins.stream()
        .filter(w -> w.getState() == State.CONFIG)
        .map(ObservableAdminWrapper::getWrapped)
        .forEach(builder::addAdminPlugin);

    PluginLoaderController.load(stage, builder);
  }

  @FXML
  private void start(MouseEvent mouseEvent) {
    start(false);
  }
}
