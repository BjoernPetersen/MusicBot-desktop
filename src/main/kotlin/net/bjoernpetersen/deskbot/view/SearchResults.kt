package net.bjoernpetersen.deskbot.view

import javafx.fxml.FXML
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.player.SongQueue
import net.bjoernpetersen.musicbot.spi.plugin.Provider

class SearchResults(
    private val finder: PluginFinder,
    private val queue: SongQueue,
    private val showAlbumArt: Boolean
) : Controller {
    @FXML
    override lateinit var root: TabPane
        private set

    var query = ""
        set(value) {
            val trimmed = value.trim()
            if (trimmed == field) return
            field = trimmed
            updateActiveTab()
        }

    private var controllers = ArrayList<SearchResult>(20)

    override fun initialize() {
        finder.providers.forEach {
            root.tabs.add(createTab(it))
        }
        root.selectionModel.selectedIndexProperty().addListener { _, _, index ->
            val controller = controllers[index.toInt()]
            controller.query = query
        }
        updateActiveTab()
    }

    private fun updateActiveTab() {
        val controller = controllers[root.selectionModel.selectedIndex]
        controller.query = query
    }

    private fun createTab(provider: Provider): Tab = Tab(provider.subject).apply {
        val controller = load(SearchResult(provider, queue, showAlbumArt))
        controllers.add(controller)
        content = controller.root
    }
}
