package net.bjoernpetersen.deskbot.view

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.layout.Region
import net.bjoernpetersen.deskbot.fximpl.SwingBrowserOpener
import net.bjoernpetersen.deskbot.lifecycle.Lifecyclist
import net.bjoernpetersen.musicbot.spi.plugin.GenericPlugin
import net.bjoernpetersen.musicbot.spi.plugin.PlaybackFactory
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import java.io.File
import kotlin.concurrent.thread

class Config : Controller {

    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var mainConfigController: MainConfig
    @FXML
    private lateinit var genericConfigController: TypeConfig
    @FXML
    private lateinit var playbackConfigController: TypeConfig
    @FXML
    private lateinit var providerConfigController: TypeConfig
    @FXML
    private lateinit var suggesterConfigController: TypeConfig

    override fun getWindowTitle(): String? = DeskBot.resources.getString("window.config")

    @FXML
    override fun initialize() {
        thread(name = "ConfigLoader", isDaemon = true) {
            val lifecycle = Lifecyclist().apply {
                create(File("plugins"))
                inject(SwingBrowserOpener())
            }
            Platform.runLater {
                mainConfigController.lifecycle = lifecycle
                genericConfigController.setType(lifecycle, GenericPlugin::class)
                playbackConfigController.setType(lifecycle, PlaybackFactory::class)
                providerConfigController.setType(lifecycle, Provider::class)
                suggesterConfigController.setType(lifecycle, Suggester::class)
            }
        }
    }
}
