package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.deskbot.api.RestApi;
import com.github.bjoernpetersen.deskbot.model.ConfigStorage;
import com.github.bjoernpetersen.deskbot.model.PlaybackFactoryWrapper;
import com.github.bjoernpetersen.deskbot.model.PluginWrapper;
import com.github.bjoernpetersen.deskbot.view.config.BaseConfigController;
import com.github.bjoernpetersen.deskbot.view.config.PluginConfigController;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.NamedPlugin;
import com.github.bjoernpetersen.jmusicbot.PlaybackFactoryManager;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.config.Config;
import com.github.bjoernpetersen.jmusicbot.provider.Provider;
import com.github.bjoernpetersen.jmusicbot.provider.Suggester;
import com.github.bjoernpetersen.jmusicbot.user.UserManager;
import java.io.File;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

public class MainController implements Window {

  private static final Logger log = Logger.getLogger(MainController.class.getName());

  @FXML
  private Parent root;

  @FXML
  private Pane centerPane;
  @FXML
  private Label pluginName;
  @FXML
  private Label pluginType;
  @FXML
  private Button closeButton;
  @FXML
  private StackPane pluginConfig;

  @FXML
  private TabPane tabPane;
  @FXML
  private ListView<PlaybackFactoryWrapper> playbackFactoryList;
  @FXML
  private ListView<Provider> providerList;
  @FXML
  private ListView<Suggester> suggesterList;
  private Map<NamedPlugin, PluginWrapper<NamedPlugin>> pluginWrappers;

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
    pluginWrappers = new HashMap<>();
    initializePluginLists();

    config = new Config(
        new ConfigStorage(new File("config.properties"), new File("secrets.properties"))
    );
    defaultSuggester = config.stringEntry(
        getClass(), "defaultSuggester",
        "", null, v -> Optional.empty()
    );

    playbackFactoryManager = new PlaybackFactoryManager(config, Collections.emptyList());
    providerManager = new ProviderManager(config, playbackFactoryManager);
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

  private void selectNull() {
    playbackFactoryList.getSelectionModel().select(null);
    providerList.getSelectionModel().select(null);
    suggesterList.getSelectionModel().select(null);
  }

  private PluginWrapper<NamedPlugin> getWrapper(Provider provider) {
    return pluginWrappers.computeIfAbsent(provider, p ->
        new PluginWrapper<>(config, providerManager, p));
  }

  private PluginWrapper<NamedPlugin> getWrapper(Suggester suggester) {
    return pluginWrappers.computeIfAbsent(suggester, s ->
        new PluginWrapper<>(config, providerManager, s));
  }

  private void fillPluginLists() {
    playbackFactoryList.getItems().addAll(playbackFactoryManager.getPlaybackFactories().stream()
        .map(PlaybackFactoryWrapper::new)
        .collect(Collectors.toList())
    );
    providerList.getItems().addAll(providerManager.getProviders().values());
    suggesterList.getItems().addAll(providerManager.getSuggesters().values());
  }

  private <T extends NamedPlugin> StringConverter<T> createStringConverter() {
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

  private <T extends NamedPlugin> ChangeListener<T> createChangeListener(String pluginType,
      Function<T, PluginConfigController> controllerFunction) {
    return (observable, oldValue, newValue) -> {
      pluginConfig.getChildren().clear();
      if (newValue == null) {
        pluginName.setText("General");
        this.pluginType.setText("");
        pluginConfig.getChildren().add(new BaseConfigController(config).createNode());
      } else {
        pluginName.setText(newValue.getReadableName());
        this.pluginType.setText(pluginType);
        pluginConfig.getChildren().add(controllerFunction.apply(newValue).createProviderConfig());
      }
      closeButton.setVisible(newValue != null);
    };
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
          PluginWrapper<NamedPlugin> wrapper = getWrapper(p);
          return new PluginConfigController(
              config,
              wrapper.activeProperty(),
              wrapper.getConfigEntries()
          );
        }
    ));
    initializePluginList(suggesterList, createChangeListener("Suggester",
        s -> {
          PluginWrapper<NamedPlugin> wrapper = getWrapper(s);
          return new PluginConfigController(
              config,
              wrapper.activeProperty(),
              wrapper.getConfigEntries()
          );
        }
    ));
  }

  private <T extends NamedPlugin> void initializePluginList(ListView<T> list,
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
    return suggesterList.getItems().stream()
        .filter(s -> getWrapper(s).isActive())
        .collect(Collectors.toList());
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
    selectNull();
    // if the wrapper for one of the providers hasn't been initialized,
    // its active state hasn't been loaded from the config
    providerList.getItems().forEach(this::getWrapper);

    Suggester defaultSuggester = askForDefaultSuggesters();
    builder.defaultSuggester(defaultSuggester);
    this.defaultSuggester.set(defaultSuggester == null ? null : defaultSuggester.getName());

    try {
      builder.userManager(new UserManager(config, "jdbc:sqlite:users.db"));
    } catch (SQLException e) {
      log.severe("Could not connect to database: " + e);
      return;
    }
    builder.apiInitializer(RestApi::new);

    PluginLoaderController.load(stage, builder);
  }
}
