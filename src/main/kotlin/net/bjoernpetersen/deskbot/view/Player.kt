package net.bjoernpetersen.deskbot.view

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.ToggleButton
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.lifecycle.Lifecyclist
import net.bjoernpetersen.musicbot.api.auth.UserManager
import net.bjoernpetersen.musicbot.api.player.PauseState
import net.bjoernpetersen.musicbot.api.player.QueueEntry
import net.bjoernpetersen.musicbot.spi.player.QueueChangeListener
import net.bjoernpetersen.musicbot.spi.player.SongQueue
import kotlin.concurrent.thread

private typealias LibPlayer = net.bjoernpetersen.musicbot.spi.player.Player

class Player(private val lifecycle: Lifecyclist) : Controller {
    private val logger = KotlinLogging.logger {}
    private val res = DeskBot.resources

    private val player: LibPlayer = lifecycle.getInjector().getInstance(LibPlayer::class.java)
    private val queue: SongQueue = lifecycle.getInjector().getInstance(SongQueue::class.java)

    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var queueList: ListView<QueueEntry>
    @FXML
    private lateinit var albumArtView: ImageView
    @FXML
    private lateinit var pauseButton: ToggleButton
    @FXML
    private lateinit var playPauseImage: ImageView
    @FXML
    private lateinit var skipButton: Button

    @FXML
    private lateinit var title: Label
    @FXML
    private lateinit var description: Label
    @FXML
    private lateinit var duration: Label
    @FXML
    private lateinit var enqueuer: Label

    @FXML
    override fun initialize() {
        Platform.runLater { stage.title = DeskBot.resources.getString("window.player") }
        setupQueue()
        player.addUiListener {
            pauseButton.isSelected = it is PauseState

            enqueuer.text = it.entry?.user?.name ?: res["description.suggested"]
            title.text = it.entry?.song?.title
            description.text = it.entry?.song?.description
            duration.text = it.entry?.song?.duration?.let { total ->
                val minutes = total / 60
                val seconds = total % 60
                "$minutes:$seconds"
            }
            albumArtView.image = it.entry?.song?.albumArtUrl?.let { url -> Image(url) }
        }
        pauseButton.selectedProperty().addListener { _, _, isSelected ->
            val path = if (isSelected) "/net/bjoernpetersen/deskbot/view/icons/play.png"
            else "/net/bjoernpetersen/deskbot/view/icons/pause.png"
            playPauseImage.image = Image(path)
        }
    }

    private fun setupQueue() {
        queueList.setCellFactory {
            TextFieldListCell(stringConverter { it?.song?.title })
        }
        queue.addUiListener(object : QueueChangeListener {
            override fun onAdd(entry: QueueEntry) {
                queueList.items.add(entry)
            }

            override fun onRemove(entry: QueueEntry) {
                queueList.items.remove(entry)
            }

            override fun onMove(entry: QueueEntry, fromIndex: Int, toIndex: Int) {
                queueList.items.remove(entry)
                queueList.items.add(toIndex, entry)
            }
        })
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun playPause(event: ActionEvent? = null) {
        pauseButton.isDisable = true
        if (pauseButton.isSelected) thread(name = "Pause", isDaemon = true) {
            player.pause()
            Platform.runLater { pauseButton.isDisable = false }
        } else thread(name = "Play", isDaemon = true) {
            player.play()
            Platform.runLater { pauseButton.isDisable = false }
        }
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun skip(event: ActionEvent? = null) {
        skipButton.isDisable = true
        thread(name = "Skip", isDaemon = true) {
            player.next()
            Platform.runLater { skipButton.isDisable = false }
        }
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun manageUsers(event: ActionEvent? = null) {
        val controller = load<UserManagement>()
        val userManager = lifecycle.getInjector().getInstance(UserManager::class.java)
        controller.setUserManager(userManager)
        controller.root.show(modal = true)
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun close(event: ActionEvent? = null) {
        lifecycle.stop()
        DeskBot.runningInstance = null
        replaceWindow(load<Overview>().root)
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun exit(event: ActionEvent? = null) {
        closeWindow()
    }
}