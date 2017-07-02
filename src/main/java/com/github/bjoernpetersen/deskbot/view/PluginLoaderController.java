package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.deskbot.model.BotHolder;
import com.github.bjoernpetersen.jmusicbot.InitStateWriter;
import com.github.bjoernpetersen.jmusicbot.InitializationException;
import com.github.bjoernpetersen.jmusicbot.Loggable;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javax.annotation.Nonnull;

public final class PluginLoaderController extends InitStateWriter implements Loggable {

  @Nonnull
  private final MusicBot.Builder builder;
  @Nonnull
  private final Stage stage;
  @Nonnull
  private final Alert alert;

  private PluginLoaderController(@Nonnull Stage stage, @Nonnull MusicBot.Builder builder) {
    this.builder = builder.initStateWriter(this);
    this.stage = stage;
    this.alert = new Alert(AlertType.INFORMATION);
    alert.setTitle("Initializing...");
    alert.getButtonTypes().clear();
    alert.getButtonTypes().add(ButtonType.CANCEL);
    alert.setResizable(true);
  }

  private void load() {
    stage.hide();
    Thread initializer = new Thread(() -> {
      try {
        BotHolder.getInstance().set(builder.build());
      } catch (IllegalStateException e) {
        logSevere(e, "Could not create MusicBot");
      } catch (InitializationException e) {
        logSevere(e, "Could not initialize MusicBot");
      } catch (InterruptedException e) {
        logWarning("Interrupted during MusicBot initialization");
      } catch (RuntimeException e) {
        logSevere(e, "Unknown error creating MusicBot");
      }
      Platform.runLater(alert::hide);
    }, "InitializationThread");
    initializer.start();
    alert.showAndWait();
    initializer.interrupt();
    if (!BotHolder.getInstance().hasValue()) {
      try {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainController.class.getResource("Main.fxml"));
        loader.load();
        Window controller = loader.getController();
        stage.show();
        controller.showOnStage(stage);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    } else {
      try {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(PlayerController.class.getResource("Player.fxml"));
        loader.load();
        Window controller = loader.getController();
        stage.show();
        controller.showOnStage(stage);
      } catch (IOException e) {
        throw new UncheckedIOException(e);
      }
    }
  }

  @Override
  public void begin(String s) {
    Platform.runLater(() -> {
      alert.setHeaderText("Loading " + s);
      alert.setContentText("");
    });
  }

  @Override
  public void state(String s) {
    Platform.runLater(() -> alert.setContentText(s));
  }

  @Override
  public void warning(String s) {
    // TODO save warnings
    Platform.runLater(() -> alert.setContentText(s));
  }

  static void load(Stage stage, MusicBot.Builder builder) {
    new PluginLoaderController(stage, builder).load();
  }
}
