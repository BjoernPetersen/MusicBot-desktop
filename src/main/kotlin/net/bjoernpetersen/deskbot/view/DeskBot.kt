package net.bjoernpetersen.deskbot.view

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.fximpl.SwingBrowserOpener
import net.bjoernpetersen.deskbot.lifecycle.Lifecyclist
import net.bjoernpetersen.deskbot.localization.YamlResourceBundle
import org.controlsfx.dialog.ExceptionDialog
import java.io.File
import java.util.ResourceBundle
import kotlin.system.exitProcess

class DeskBot : Application() {
    private val logger = KotlinLogging.logger {}

    init {
        application = this
    }

    override fun start(primaryStage: Stage) {
        if (parameters.unnamed.contains("--start")) {
            instantStart(primaryStage)
        } else {
            primaryStage.scene = Scene(load<Overview>().root)
            primaryStage.show()
        }
    }

    private fun instantStart(stage: Stage) {
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
                        exitProcess(1)
                    } else {
                        val playerUi = Player(cycle)
                        stage.scene = Scene(load(playerUi).root)
                        stage.show()
                    }
                }
            }
        } catch (e: Throwable) {
            ExceptionDialog(e).apply {
                headerText = "Could not initialize bot"
            }.showAndWait()
            exitProcess(1)
        }
    }

    override fun stop() {
        runningInstance?.apply {
            if (stage == Lifecyclist.Stage.Running) {
                Thread { stop() }.start()
            }
        }
    }

    companion object {
        var runningInstance: Lifecyclist? = null
        lateinit var application: DeskBot
            private set
        val resources: ResourceBundle
            get() = ResourceBundle.getBundle(DeskBot::class.java.name, YamlResourceBundle.Control)

        @Suppress("SpreadOperator")
        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(DeskBot::class.java, *args)
        }
    }
}
