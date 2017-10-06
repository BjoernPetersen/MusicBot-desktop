package com.github.bjoernpetersen.deskbot;

import com.github.bjoernpetersen.deskbot.model.BotHolder;
import com.github.bjoernpetersen.deskbot.view.MainController;
import com.github.bjoernpetersen.jmusicbot.Loggable;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class DeskBot extends Application implements Loggable {

  private static DeskBot instance;

  static {
    try (InputStream in = DeskBot.class.getResourceAsStream("/logging.properties")) {
      LogManager.getLogManager().readConfiguration(in);
    } catch (IOException e) {
      System.out.println("Can't initialize logging!");
      System.exit(100);
    }
  }

  public DeskBot() {
    instance = this;
  }

  public static DeskBot getInstance() {
    return instance;
  }

  @Override
  public void start(Stage primaryStage) throws Exception {
    primaryStage.setTitle("DeskBot");
    initBotListener();
    primaryStage.setOnHiding(event -> BotHolder.getInstance().set(null));

    FXMLLoader loader = new FXMLLoader(MainController.class.getResource("Main.fxml"));
    loader.load();
    MainController controller = loader.getController();
    controller.showOnStage(primaryStage);
    primaryStage.show();
    if (getParameters().getUnnamed().contains("--no-config")) {
      controller.start(true);
    }
  }

  private void initBotListener() {
    BotHolder.getInstance().botProperty().addListener((observable, oldValue, newValue) -> {
      if (oldValue != null) {
        logInfo("Closing MusicBot...");
        try {
          oldValue.close();
          logInfo("MusicBot closed.");
        } catch (IOException e) {
          logSevere(e, "Couldn't close MusicBot");
        } catch (Throwable t) {
          logSevere(t, "Unknown error closing MusicBot");
        }
      }
    });
  }

  public static void main(String[] args) {
    try {
      Application.launch(args);
    } catch (Throwable t) {
      Logger.getLogger(DeskBot.class.getName()).log(Level.SEVERE, "Unhandled exception", t);
      throw t;
    }
  }
}
