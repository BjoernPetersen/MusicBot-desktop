package net.bjoernpetersen.deskbot.view

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.TextField
import javafx.scene.control.ToggleButton
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.impl.toDurationString
import net.bjoernpetersen.deskbot.lifecycle.Lifecyclist
import net.bjoernpetersen.musicbot.api.auth.UserManager
import net.bjoernpetersen.musicbot.api.player.PauseState
import net.bjoernpetersen.musicbot.api.player.QueueEntry
import net.bjoernpetersen.musicbot.api.player.StopState
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.player.PlayerStateListener
import net.bjoernpetersen.musicbot.spi.player.QueueChangeListener
import net.bjoernpetersen.musicbot.spi.player.SongQueue
import kotlin.coroutines.CoroutineContext

private typealias LibPlayer = net.bjoernpetersen.musicbot.spi.player.Player

class Player(private val lifecycle: Lifecyclist) : Controller, CoroutineScope {
    private val logger = KotlinLogging.logger {}
    private val res = DeskBot.resources

    private lateinit var job: Job
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    private val player: LibPlayer = lifecycle.getInjector().getInstance(LibPlayer::class.java)
    private val queue: SongQueue = lifecycle.getInjector().getInstance(SongQueue::class.java)
    private val finder: PluginFinder = lifecycle.getPluginFinder()

    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var leftSpace: StackPane
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
    private lateinit var searchField: TextField

    @FXML
    override fun initialize() {
        job = Job()
        Platform.runLater { stage.title = DeskBot.resources.getString("window.player") }
        setupQueue()

        val playerStateListener: PlayerStateListener = { _, it ->
            pauseButton.isSelected = it is PauseState

            enqueuer.text = it.entry?.let { entry ->
                entry.user?.name ?: res["description.suggested"]
            }
            title.text = it.entry?.song?.title
            description.text = it.entry?.song?.description
            duration.text = it.entry?.song?.duration?.let { it.toDurationString() }
            albumArtView.image = it.entry?.song?.albumArtUrl?.let { url -> Image(url) }
        }
        player.addUiListener(playerStateListener)
        playerStateListener(StopState, player.state)

        pauseButton.selectedProperty().addListener { _, _, isSelected ->
            val path = if (isSelected) "/net/bjoernpetersen/deskbot/view/icons/play.png"
            else "/net/bjoernpetersen/deskbot/view/icons/pause.png"
            playPauseImage.image = Image(path)
        }

        setupSearch()
    }

    private fun setupQueue() {
        queueList.setCellFactory {
            load<QueueEntryListCell>().apply {
                dragHandler = { fromIndex, toIndex ->
                    val entry = queueList.items[fromIndex]
                    queue.move(entry.song, toIndex)
                    Platform.runLater {
                        // We defer the selection so the QueueChangeListener will get called first
                        queueList.selectionModel.select(entry)
                    }
                    true
                }
                val removeItem = MenuItem("Remove").apply {
                    setOnAction { event ->
                        item?.song?.let { queue.remove(it) }
                        event.consume()
                    }
                }
                contextMenu = ContextMenu(removeItem).apply {
                    setOnShown {
                        if (item == null) Platform.runLater { hide() }
                        it.consume()
                    }
                }
            }
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
        queue.toList().forEach { queueList.items.add(it) }
    }

    private fun setupSearch() {
        val searchResults = load(SearchResults(finder, queue))
        leftSpace.children.add(searchResults.root)
        searchResults.root.isVisible = false
        searchField.textProperty().addListener { _, _, new ->
            if (new.isBlank()) {
                searchResults.query = ""
                searchResults.root.isVisible = false
            } else {
                searchResults.query = new
                searchResults.root.isVisible = true
            }
        }
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun playPause(event: ActionEvent? = null) {
        pauseButton.isDisable = true
        if (pauseButton.isSelected) launch {
            player.pause()
            withContext(Dispatchers.Main) { pauseButton.isDisable = false }
        } else launch {
            player.play()
            withContext(Dispatchers.Main) { pauseButton.isDisable = false }
        }
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun skip(event: ActionEvent? = null) {
        skipButton.isDisable = true
        launch {
            player.next()
            withContext(Dispatchers.Main) { skipButton.isDisable = false }
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
        job.cancel()
        lifecycle.stop()
        DeskBot.runningInstance = null
        replaceWindow(load<Overview>().root)
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun exit(event: ActionEvent? = null) {
        job.cancel()
        closeWindow()
    }
}
