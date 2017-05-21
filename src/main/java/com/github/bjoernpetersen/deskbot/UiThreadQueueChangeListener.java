package com.github.bjoernpetersen.deskbot;

import com.github.bjoernpetersen.jmusicbot.Song;
import com.github.bjoernpetersen.jmusicbot.playback.QueueChangeListener;
import javafx.application.Platform;
import javax.annotation.Nonnull;

public final class UiThreadQueueChangeListener implements QueueChangeListener {

  @Nonnull
  private final QueueChangeListener wrapped;

  public UiThreadQueueChangeListener(@Nonnull QueueChangeListener wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public void onAdd(Song song) {
    Platform.runLater(() -> wrapped.onAdd(song));
  }

  @Override
  public void onRemove(Song song) {
    Platform.runLater(() -> wrapped.onRemove(song));
  }
}
