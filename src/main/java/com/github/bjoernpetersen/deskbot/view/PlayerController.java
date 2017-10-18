package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.deskbot.UiThreadPlayerStateListener;
import com.github.bjoernpetersen.deskbot.UiThreadQueueChangeListener;
import com.github.bjoernpetersen.deskbot.model.BotHolder;
import com.github.bjoernpetersen.jmusicbot.Loggable;
import com.github.bjoernpetersen.jmusicbot.MusicBot;
import com.github.bjoernpetersen.jmusicbot.Song;
import com.github.bjoernpetersen.jmusicbot.playback.Player;
import com.github.bjoernpetersen.jmusicbot.playback.PlayerState;
import com.github.bjoernpetersen.jmusicbot.playback.PlayerState.State;
import com.github.bjoernpetersen.jmusicbot.playback.PlayerStateListener;
import com.github.bjoernpetersen.jmusicbot.playback.Queue;
import com.github.bjoernpetersen.jmusicbot.playback.QueueChangeListener;
import com.github.bjoernpetersen.jmusicbot.playback.QueueEntry;
import com.github.bjoernpetersen.jmusicbot.playback.SongEntry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.logging.Logger;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.controlsfx.control.Notifications;

public class PlayerController implements Loggable, Window {

  @Nonnull
  private final Logger logger;

  @FXML
  private Parent root;
  @FXML
  private Label currentTitle;
  @FXML
  private Label currentDescription;
  @FXML
  private Label currentDuration;
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
  private boolean showNotifications = false;

  public PlayerController() {
    this.logger = createLogger();
  }

  @Override
  @Nonnull
  public Logger getLogger() {
    return logger;
  }

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

    player.addListener(playerListener = new UiThreadPlayerStateListener(new PlayerStateListener() {
      @Nullable
      private Song last;

      @Override
      public void onChanged(@Nonnull PlayerState state) {
        if (state.getEntry().isPresent()) {
          SongEntry entry = state.getEntry().get();
          Song song = entry.getSong();
          if (song.equals(last)) {
            return;
          }
          last = song;
          currentTitle.setText(song.getTitle());
          currentDescription.setText(song.getDescription());

          int duration = song.getDuration();
          int seconds = duration % 60;
          int minutes = (duration - seconds) / 60;
          String durationText = String.format("%d:%02d", minutes, seconds);
          currentDuration.setText(durationText);

          if (showNotifications) {
            Notifications notifications = Notifications.create()
                .hideAfter(Duration.seconds(5))
                .title(song.getTitle())
                .text(song.getDescription() + '\n' + durationText);
            song.getAlbumArtUrl().ifPresent(url -> {
              ImageView imageView = new ImageView(url);
              imageView.setFitHeight(80);
              imageView.setPreserveRatio(true);
              notifications.graphic(imageView);
            });
            notifications.show();
          }
        } else {
          currentTitle.setText(null);
          currentDescription.setText(null);
          currentDuration.setText(null);
        }
      }
    }));
    playerListener.onChanged(player.getState());

    initializeQueueListener();
  }

  private void initializeQueueListener() {
    queue = FXCollections.observableArrayList();
    player.getQueue().addListener(queueListener = new UiThreadQueueChangeListener(
        new QueueChangeListener() {
          @Override
          public void onAdd(@Nonnull QueueEntry entry) {
            queue.add(entry.getSong());
          }

          @Override
          public void onRemove(@Nonnull QueueEntry entry) {
            queue.remove(entry.getSong());
          }
        })
    );

    queueList.setCellFactory(new CellFactory());

    queueList.setItems(queue);
  }


  @FXML
  private void next(MouseEvent event) {
    try {
      player.next();
    } catch (InterruptedException e) {
      logWarning(e, "Interrupted calling next");
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

  public void setShowNotifications(boolean showNotifications) {
    this.showNotifications = showNotifications;
  }

  private class CellFactory implements Callback<ListView<Song>, ListCell<Song>> {

    private final Callback<ListView<Song>, ListCell<Song>> wrapped;

    CellFactory() {
      this.wrapped = TextFieldListCell.forListView(new StringConverter<Song>() {
        @Override
        public String toString(Song object) {
          return object.getTitle();
        }

        @Override
        public Song fromString(String string) {
          return null;
        }
      });
    }

    @Override
    public ListCell<Song> call(ListView<Song> param) {
      ListCell<Song> cell = wrapped.call(param);
      ContextMenu menu = new ContextMenu();
      MenuItem removeButton = new MenuItem("Remove");
      removeButton.setOnAction(event -> {
        Song song = cell.getItem();
        if (song != null) {
          Queue queue = player.getQueue();
          queue.toList().stream()
              .filter(e -> e.getSong().equals(song))
              .findAny()
              .ifPresent(queue::remove);
        }
      });
      menu.getItems().add(removeButton);
      menu.setOnShowing(event -> {
        if (cell.getItem() == null) {
          event.consume();
        }
      });
      cell.setContextMenu(menu);
      return cell;
    }
  }
}
