package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.deskbot.UiThreadPlayerStateListener;
import com.github.bjoernpetersen.deskbot.UiThreadQueueChangeListener;
import com.github.bjoernpetersen.deskbot.model.BotHolder;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.Song;
import com.github.bjoernpetersen.jmusicbot.playback.Player;
import com.github.bjoernpetersen.jmusicbot.playback.PlayerState.State;
import com.github.bjoernpetersen.jmusicbot.playback.PlayerStateListener;
import com.github.bjoernpetersen.jmusicbot.playback.Queue;
import com.github.bjoernpetersen.jmusicbot.playback.QueueChangeListener;
import com.github.bjoernpetersen.jmusicbot.playback.SongEntry;
import java.io.IOException;
import java.io.UncheckedIOException;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import javax.annotation.Nonnull;

public class PlayerController implements Window {

  @FXML
  private Parent root;
  @FXML
  private Label currentTitle;
  @FXML
  private ListView<Song> queueList;
  private ObservableList<Song> queue;
  private QueueChangeListener queueListener;

  private boolean autoPause = false;
  @FXML
  private ToggleButton pauseToggle;
  @FXML
  private VBox spacer;

  private Stage stage;
  private MusicBot musicBot;
  private Player player;
  private InvalidationListener botListener;
  private PlayerStateListener playerListener;

  @FXML
  private void initialize() {
    HBox.setHgrow(spacer, Priority.ALWAYS);
    musicBot = BotHolder.getInstance().getValue();
    player = musicBot.getPlayer();
    BotHolder.getInstance().botProperty().addListener(botListener = observable -> exit(null));

    player.addListener(new UiThreadPlayerStateListener(
        state -> {
          autoPause = true;
          pauseToggle.setSelected(state.getState() == State.PAUSE);
          autoPause = false;
        }
    ));
    pauseToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (!autoPause) {
        if (newValue) {
          player.pause();
        } else {
          player.play();
        }
      }
    });

    player.addListener(playerListener = new UiThreadPlayerStateListener(state -> {
      currentTitle.setText(state.getEntry()
          .map(SongEntry::getSong)
          .map(Song::getTitle)
          .orElse(null));
    }));

    initializeQueueListener();
  }

  private void initializeQueueListener() {
    queue = FXCollections.observableArrayList();
    player.getQueue().addListener(queueListener = new UiThreadQueueChangeListener(
        new QueueChangeListener() {
          @Override
          public void onAdd(@Nonnull Queue.Entry entry) {
            queue.add(entry.getSong());
          }

          @Override
          public void onRemove(@Nonnull Queue.Entry entry) {
            queue.remove(entry.getSong());
          }
        })
    );

    queueList.setCellFactory(TextFieldListCell.forListView(new StringConverter<Song>() {
      @Override
      public String toString(Song object) {
        return object.getTitle();
      }

      @Override
      public Song fromString(String string) {
        return null;
      }
    }));

    queueList.setItems(queue);
  }


  @FXML
  private void next(MouseEvent event) {
    try {
      player.next();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  @FXML
  private void exit(MouseEvent event) {
    BotHolder.getInstance().set(null);
    BotHolder.getInstance().botProperty().removeListener(botListener);
    player.getQueue().removeListener(queueListener);
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(MainController.class.getResource("Main.fxml"));
    try {
      loader.load();
      Window window = loader.getController();
      window.showOnStage(stage);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @FXML
  private void manageUsers(MouseEvent mouseEvent) {
    Parent parent = new UserController(musicBot.getUserManager()).createNode();
    Dialog<Void> dialog = new Dialog<>();
    DialogPane dialogPane = new DialogPane();
    dialogPane.setContent(parent);
    dialogPane.getButtonTypes().add(ButtonType.OK);
    dialog.setDialogPane(dialogPane);
    dialog.setTitle("Manage users");
    dialog.showAndWait();
  }

  @Override
  public void showOnStage(Stage stage) {
    this.stage = stage;
    stage.setScene(new Scene(root));
  }
}
