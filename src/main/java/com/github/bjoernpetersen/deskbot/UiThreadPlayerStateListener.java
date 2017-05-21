package com.github.bjoernpetersen.deskbot;

import com.github.bjoernpetersen.jmusicbot.playback.PlayerState;
import com.github.bjoernpetersen.jmusicbot.playback.PlayerStateListener;
import javafx.application.Platform;
import javax.annotation.Nonnull;

/**
 * Wrapper for PlayerStateListener implementations which calls the wrapped instance on the JavaFX
 * application thread.
 */
public final class UiThreadPlayerStateListener implements PlayerStateListener {

  private final PlayerStateListener listener;

  /**
   * Creates a new UiThreadPlayerStateListener wrapping the specified PlayerStateListener. The
   * wrapped listener will be called on the JavaFX application thread.
   *
   * @param listener any PlayerStateListener
   */
  public UiThreadPlayerStateListener(PlayerStateListener listener) {
    this.listener = listener;
  }

  @Override
  public void onChanged(@Nonnull PlayerState playerState) {
    Platform.runLater(() -> listener.onChanged(playerState));
  }
}
