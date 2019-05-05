package net.bjoernpetersen.deskbot.view

import javafx.application.Platform
import net.bjoernpetersen.musicbot.api.player.QueueEntry
import net.bjoernpetersen.musicbot.spi.player.Player
import net.bjoernpetersen.musicbot.spi.player.PlayerStateListener
import net.bjoernpetersen.musicbot.spi.player.QueueChangeListener
import net.bjoernpetersen.musicbot.spi.player.SongQueue

private class UiThreadQueueChangeListener(private val wrapped: QueueChangeListener) :
    QueueChangeListener {

    override fun onAdd(entry: QueueEntry) {
        Platform.runLater { wrapped.onAdd(entry) }
    }

    override fun onRemove(entry: QueueEntry) {
        Platform.runLater { wrapped.onRemove(entry) }
    }

    override fun onMove(entry: QueueEntry, fromIndex: Int, toIndex: Int) {
        Platform.runLater { wrapped.onMove(entry, fromIndex, toIndex) }
    }
}

fun PlayerStateListener.uiThread(): PlayerStateListener = { Platform.runLater { this(it) } }
fun Player.addUiListener(listener: PlayerStateListener) {
    addListener(listener.uiThread())
}

fun QueueChangeListener.uiThread(): QueueChangeListener = UiThreadQueueChangeListener(
    this)
fun SongQueue.addUiListener(listener: QueueChangeListener) {
    addListener(listener.uiThread())
}
