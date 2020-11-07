package net.bjoernpetersen.deskbot.view

import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.layout.Region
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.fximpl.SwingBrowserOpener
import net.bjoernpetersen.deskbot.lifecycle.Lifecyclist
import net.bjoernpetersen.musicbot.spi.plugin.GenericPlugin
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import org.controlsfx.dialog.ExceptionDialog
import java.io.File
import kotlin.concurrent.thread

class Overview : Controller {

    private val logger = KotlinLogging.logger {}

    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var genericActivationController: Activation
    @FXML
    private lateinit var providerActivationController: Activation
    @FXML
    private lateinit var suggesterActivationController: Activation

    @FXML
    override fun initialize() {
        initActivation()
    }

    override fun getWindowTitle(): String? = DeskBot.resources["window.overview"]

    private fun initActivation() {
        thread(name = "OverviewLoader", isDaemon = true) {
            val lifecycle = Lifecyclist()
            lifecycle.create(File("plugins"))
            val manager = lifecycle.getDependencyManager()
            Platform.runLater {
                genericActivationController.setData<GenericPlugin>(manager)
                providerActivationController.setData<Provider>(manager)
                suggesterActivationController.setData<Suggester>(manager)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    private fun onConfig(event: ActionEvent? = null) {
        logger.debug { "Configure button pressed." }
        load<Config>().show(modal = true)
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    private fun start(event: ActionEvent? = null) {
        logger.debug { "Start button pressed." }
        val cycle = Lifecyclist()
        try {
            cycle.create(File("plugins"))
            cycle.inject(SwingBrowserOpener())
            runBlocking {
                cycle.run {
                    if (it != null) {
                        logger.error(it) { "Failed to start" }
                        ExceptionDialog(it).apply {
                            headerText = "An exception occurred while starting the bot"
                        }.showAndWait()
                        load<Overview>().show()
                    } else {
                        val playerUi = Player(cycle)
                        replaceWindow(load(playerUi).root)
                    }
                }
            }
        } catch (e: Throwable) {
            ExceptionDialog(e).apply {
                headerText = "Could not initialize bot"
            }.show()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    private fun exit(event: ActionEvent? = null) {
        logger.debug { "Exit button pressed." }
        Platform.exit()
    }
}
