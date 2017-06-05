package com.github.bjoernpetersen.deskbot;

import com.github.bjoernpetersen.jmusicbot.playback.Queue;
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
  public void onAdd(@Nonnull Queue.Entry entry) {
    Platform.runLater(() -> wrapped.onAdd(entry));
  }

  @Override
  public void onRemove(@Nonnull Queue.Entry entry) {
    Platform.runLater(() -> wrapped.onRemove(entry));
  }
}
