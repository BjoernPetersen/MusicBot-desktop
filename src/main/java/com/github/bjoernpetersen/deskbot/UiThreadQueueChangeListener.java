package com.github.bjoernpetersen.deskbot;

import com.github.bjoernpetersen.jmusicbot.playback.QueueChangeListener;
import com.github.bjoernpetersen.jmusicbot.playback.QueueEntry;
import javafx.application.Platform;
import javax.annotation.Nonnull;

public final class UiThreadQueueChangeListener implements QueueChangeListener {

  @Nonnull
  private final QueueChangeListener wrapped;

  public UiThreadQueueChangeListener(@Nonnull QueueChangeListener wrapped) {
    this.wrapped = wrapped;
  }

  @Override
  public void onAdd(@Nonnull QueueEntry entry) {
    Platform.runLater(() -> wrapped.onAdd(entry));
  }

  @Override
  public void onRemove(@Nonnull QueueEntry entry) {
    Platform.runLater(() -> wrapped.onRemove(entry));
  }
}
