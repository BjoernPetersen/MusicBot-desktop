package com.github.bjoernpetersen.deskbot.view

import com.github.bjoernpetersen.deskbot.UiThreadPlayerStateListener
import com.github.bjoernpetersen.deskbot.UiThreadQueueChangeListener
import com.github.bjoernpetersen.jmusicbot.Loggable
import com.github.bjoernpetersen.jmusicbot.MusicBot
import com.github.bjoernpetersen.jmusicbot.playback.PlayState
import com.github.bjoernpetersen.jmusicbot.playback.QueueChangeListener
import com.github.bjoernpetersen.jmusicbot.playback.QueueEntry
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException
import javafx.collections.ListChangeListener
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.text.Font
import tornadofx.*
import java.io.File
import java.io.IOException
import java.util.*

class PlayerWindow : Fragment("DeskBot Player"), Loggable {
  val bot: MusicBot by param()

  override val root = borderpane {
    left = listview<QueueEntry>(bot.player.queue.toList().toMutableList().observable()) {
      bot.player.queue.addListener(UiThreadQueueChangeListener(object : QueueChangeListener {
        override fun onAdd(entry: QueueEntry) {
          items.add(entry)
        }

        override fun onRemove(entry: QueueEntry) {
          items.remove(entry)
        }

        override fun onMove(entry: QueueEntry, from: Int, to: Int) {
          items.move(entry, to)
        }
      }))
      cellFormat {
        text = "${it.song.title} - ${it.song.description}"
        contextMenu = ContextMenu().also { menu ->
          val removeButton = MenuItem("Remove")
          removeButton.setOnAction { _ ->
            val song = item?.song
            if (song != null) {
              val queue = bot.player.queue
              queue.toList().asSequence()
                  .firstOrNull { it.song == song }
                  ?.apply { queue.remove(it) }
            }
          }
          menu.items.add(removeButton)
          menu.setOnShowing({ event ->
            if (item == null) {
              event.consume()
            }
          })
        }
      }
      items.addListener(ListChangeListener { dumpQueue() })
      restoreQueue()
    }
    bottom = toolbar {
      togglebutton("Pause") {
        var autoSelect = false
        bot.player.addListener(UiThreadPlayerStateListener {
          autoSelect = true
          isSelected = it !is PlayState
          autoSelect = false
        })
        isSelected = bot.player.state !is PlayState
        selectedProperty().addListener { _, _, pause ->
          if (!autoSelect) {
            if (pause) bot.player.pause()
            else bot.player.play()
          }
        }
      }
      button("Next") {
        setOnAction { bot.player.next() }
      }
      vbox {
        HBox.setHgrow(this, Priority.ALWAYS)
      }
      button("Manage users") {
        setOnAction {
          val parent = UserController(bot.userManager).createNode()
          val dialog = Dialog<Void>()
          val dialogPane = DialogPane()
          dialogPane.content = parent
          dialogPane.buttonTypes.add(ButtonType.OK)
          dialog.dialogPane = dialogPane
          dialog.title = "Manage users"
          dialog.showAndWait()
        }
      }
      button("Close") {
        setOnAction {
          bot.close()
          primaryStage.onHiding = null
          find<ConfigController>().reload()
          replaceWith<ConfigWindow>()
        }
      }
    }
    center = stackpane {
      alignment = Pos.CENTER
      paddingAll = 40.0
      vbox {
        alignment = Pos.CENTER
        val albumArt: ImageView = imageview {
          fitHeight = 250.0
          fitWidth = 250.0
          isSmooth = true
          isPreserveRatio = true
        }
        val title = label { font = Font.font(18.0) }
        val description = label { font = Font.font(18.0) }
        val duration = label { font = Font.font(18.0) }
        val enqueuer = label { font = Font.font(18.0) }
        val listener = UiThreadPlayerStateListener {
          val entry = it.entry
          albumArt.image = entry?.song?.albumArtUrl?.orElse(null)?.let { Image(it) }
          title.text = entry?.song?.title
          description.text = entry?.song?.description
          duration.text = entry?.song?.duration?.asDuration()
          enqueuer.text = entry?.let { it.user?.name ?: "Suggested" }
        }
        bot.player.addListener(listener)
        listener.onChanged(bot.player.state)
      }
    }
  }

  override fun onDock() {
    super.onDock()
    primaryStage.sizeToScene()
  }

  private fun dumpQueue() {
    val dump = File("queue.dump")
    try {
      dump.bufferedWriter().use { writer ->
        bot.player.queue.toList().asSequence()
            .map { it.song }
            .map { "${it.id};${it.provider.id}" }
            .forEach {
              try {
                writer.write(it)
                writer.newLine()
              } catch (e: IOException) {
                // ignore
              }
            }
      }
    } catch (e: IOException) {
      logInfo(e, "Could not dump queue")
    }
  }

  private fun restoreQueue() {
    val dump = File("queue.dump")
    if (!dump.isFile) {
      logFinest("No dump file found")
      return
    }

    try {
      dump.bufferedReader().use { reader ->
        reader.lineSequence()
            .map { it.split(';') }
            .filter { it.size == 2 }
            .map { s ->
              val songId = s[0]
              val providerId = s[1]
              val provider = bot.providerManager.getProvider(providerId)
              if (provider != null) {
                try {
                  provider.lookup(songId)
                } catch (e: NoSuchSongException) {
                  null
                }
              } else null
            }
            .filterNotNull()
            .map { QueueEntry(it, bot.userManager.botUser) }
            .forEach { bot.player.queue.append(it) }
      }
    } catch (e: IOException) {
      logFinest("Dump file could not be read")
    }
  }

  init {
    primaryStage.setOnHiding {
      bot.close()
      logInfo("Closed MusicBot")
    }
  }
}

fun Int.asDuration(): String? {
  if (this == 0) {
    return null
  }
  val seconds = this % 60
  val minutes = (this - seconds) / 60
  return String.format(Locale.US, "%d:%02d", minutes, seconds)
}
