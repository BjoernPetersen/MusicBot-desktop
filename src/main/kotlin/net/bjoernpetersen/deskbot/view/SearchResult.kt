package net.bjoernpetersen.deskbot.view

import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.layout.StackPane
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.bjoernpetersen.musicbot.api.auth.BotUser
import net.bjoernpetersen.musicbot.api.player.QueueEntry
import net.bjoernpetersen.musicbot.api.player.Song
import net.bjoernpetersen.musicbot.spi.player.SongQueue
import net.bjoernpetersen.musicbot.spi.plugin.Provider

class SearchResult(
    private val provider: Provider,
    private val queue: SongQueue
) : Controller, CoroutineScope by MainScope() {

    @FXML
    override lateinit var root: StackPane
        private set

    @FXML
    private lateinit var resultList: ListView<Song>

    private var updateJob: Job = Job().apply { complete() }
        set(value) {
            if (field.isActive) field.cancel()
            field = value
        }

    var query = ""
        set(value) {
            val trimmed = value.trim()
            if (trimmed == field) return
            field = trimmed
            updateJob = launch {
                update()
            }
        }

    private suspend fun update() {
        val query = query
        if (query.isBlank()) return
        coroutineScope {
            resultList.items.clear()
            val result = withContext(Dispatchers.IO) {
                provider.search(query)
            }
            resultList.items.addAll(result)
        }
    }

    override fun initialize() {
        resultList.setCellFactory {
            load<SongListCell>().apply {
                root.setOnMouseClicked {
                    val song = item
                    if (song != null && it.clickCount > 1) {
                        queue.insert(QueueEntry(song, BotUser))
                        resultList.items.remove(song)
                        resultList.selectionModel.select(null)
                    }
                }
            }
        }
    }
}
