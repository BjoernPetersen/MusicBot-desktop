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
import javafx.scene.layout.VBox
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.impl.localAlbumArtUrl
import net.bjoernpetersen.deskbot.impl.toDurationString
import net.bjoernpetersen.deskbot.lifecycle.Lifecyclist
import net.bjoernpetersen.musicbot.api.auth.UserManager
import net.bjoernpetersen.musicbot.api.player.PlayState
import net.bjoernpetersen.musicbot.api.player.ProgressTracker
import net.bjoernpetersen.musicbot.api.player.QueueEntry
import net.bjoernpetersen.musicbot.api.player.Song
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
    private val timeTracker: ProgressTracker =
        lifecycle.getInjector().getInstance(ProgressTracker::class.java)
    private val albumArtMode = lifecycle.getMainConfig().loadAlbumArt.get()!!

    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var leftSpace: StackPane
    @FXML
    private lateinit var queueList: ListView<QueueEntry>

    @FXML
    private lateinit var currentSongBox: VBox
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

    override fun getWindowTitle(): String? = res.getString("window.player")

    @FXML
    override fun initialize() {
        job = Job()
        setupQueue()

        var song: Song? = null
        launch(Dispatchers.Main) {
            while (isActive) {
                val fullDuration = song?.duration?.toDurationString()
                    ?: res.getString("player.unknownDuration")
                val progress = timeTracker.getCurrentProgress().duration.seconds
                    .toInt().toDurationString()
                duration.text = "$progress / $fullDuration"
                // Don't spam these calls too much
                @Suppress("MagicNumber")
                delay(50)
            }
        }

        if (!albumArtMode.showCurrent)
            currentSongBox.children.remove(albumArtView)

        val playerStateListener: PlayerStateListener = { _, it ->
            pauseButton.isSelected = it !is PlayState

            enqueuer.text = it.entry?.let { entry ->
                entry.user?.name ?: res["player.suggested"]
            }
            song = it.entry?.song
            title.text = song?.title
            description.text = song?.description?.substringBefore('\n')
            duration.text = null
            if (albumArtMode.showCurrent)
                albumArtView.image = song?.localAlbumArtUrl?.let { url -> Image(url) }
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
                if (!albumArtMode.showListItem) {
                    removeAlbumArt()
                }
                dragHandler = { fromIndex, toIndex ->
                    val entry = queueList.items[fromIndex]
                    queue.move(entry.song, toIndex)
                    Platform.runLater {
                        // We defer the selection so the QueueChangeListener will get called first
                        queueList.selectionModel.select(entry)
                    }
                    true
                }
                val removeItem = MenuItem(DeskBot.resources["action.remove"]).apply {
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
        val searchResults = load(SearchResults(finder, queue, albumArtMode.showListItem))
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
        controller.show(modal = true)
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
