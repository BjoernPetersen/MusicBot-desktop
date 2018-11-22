package net.bjoernpetersen.deskbot.fximpl

import javafx.application.Platform
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.view.DeskBot
import net.bjoernpetersen.deskbot.view.get
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.category
import net.bjoernpetersen.musicbot.spi.plugin.management.InitStateWriter

class FxInitStateWriter(
    private val updateTitle: (String) -> Unit,
    private val updateMessage: (String) -> Unit) : InitStateWriter {

    private val res = DeskBot.resources
    private val logger = KotlinLogging.logger { }
    private lateinit var plugin: Plugin

    override fun begin(plugin: Plugin) {
        Platform.runLater {
            updateTitle(res["task.plugin.title"].format(plugin.category.simpleName, plugin.name))
        }
    }

    override fun state(state: String) {
        Platform.runLater {
            updateMessage(state)
        }
    }

    override fun warning(warning: String) {
        Platform.runLater {
            updateMessage("WARNING: $warning")
            logger.warn { "${plugin.describe()}: $warning" }
        }
    }

    override fun close() {
        Platform.runLater {
            updateMessage("Done.")
        }
    }
}

private fun Plugin.describe(): String {
    return "${category.simpleName} $name"
}
