package com.github.bjoernpetersen.deskbot

import com.github.bjoernpetersen.jmusicbot.playback.QueueChangeListener
import com.github.bjoernpetersen.jmusicbot.playback.QueueEntry
import javafx.application.Platform

class UiThreadQueueChangeListener(private val wrapped: QueueChangeListener) : QueueChangeListener {

  override fun onAdd(entry: QueueEntry) {
    Platform.runLater { wrapped.onAdd(entry) }
  }

  override fun onRemove(entry: QueueEntry) {
    Platform.runLater { wrapped.onRemove(entry) }
  }

  override fun onMove(entry: QueueEntry, from: Int, to: Int) {
    Platform.runLater { wrapped.onMove(entry, from, to) }
  }
}
