package net.bjoernpetersen.deskbot.view

import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.lifecycle.Lifecyclist
import net.bjoernpetersen.deskbot.localization.YamlResourceBundle
import java.util.ResourceBundle

class DeskBot : Application() {
    private val logger = KotlinLogging.logger {}

    override fun start(primaryStage: Stage) {
        primaryStage.title = "DeskBot"

        primaryStage.scene = Scene(load<Overview>().root)
        primaryStage.show()
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
        val resources: ResourceBundle
            get() = ResourceBundle.getBundle(DeskBot::class.java.name, YamlResourceBundle.Control)

        @JvmStatic
        fun main(args: Array<String>) {
            Application.launch(DeskBot::class.java, *args)
        }
    }
}
