package net.bjoernpetersen.deskbot.view.property

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ToolBar
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.view.DeskBot
import net.bjoernpetersen.deskbot.view.stringConverter
import net.bjoernpetersen.musicbot.api.config.ChoiceBox
import org.controlsfx.property.editor.AbstractPropertyEditor
import kotlin.concurrent.thread

private typealias FxChoiceBox<T> = javafx.scene.control.ChoiceBox<T>

class ChoiceBoxPropertyEditor<T : Any>(
    private val item: ConfigEntryItem<T>,
    private val configNode: ChoiceBox<T>
) : AbstractPropertyEditor<T, Node>(item, node<T>()) {

    private val logger = KotlinLogging.logger { }

    private val button: Button
        get() = (editor as ToolBar).items[0] as Button

    @Suppress("UNCHECKED_CAST")
    private val choiceBox: FxChoiceBox<T>
        get() = (editor as ToolBar).items[1] as FxChoiceBox<T>

    init {
        choiceBox.converter = stringConverter { it?.let(configNode.descriptor) }
        button.setOnAction { refresh() }
        if (configNode.lazy) {
            val value = item.entry.get()
            if (value != null) {
                choiceBox.items.add(value)
                choiceBox.selectionModel.select(value)
            }
        } else refresh()
    }

    private fun refresh() {
        button.isDisable = true
        thread(name = "Refresh ${item.entry.key}", isDaemon = true) {
            val data = try {
                configNode.refresh()
            } catch (e: Throwable) {
                logger.error(e) { "Action button failed" }
                null
            }

            Platform.runLater {
                choiceBox.isDisable = false
                button.isDisable = false
                // TODO handle failure
                if (data != null) {
                    val active = item.entry.get()
                    choiceBox.items.setAll(data)
                    choiceBox.selectionModel.select(active)
                }
            }
        }
    }

    override fun getObservableValue(): ObservableValue<T> {
        return choiceBox.selectionModel.selectedItemProperty()
    }

    override fun setValue(value: T?) {
        choiceBox.selectionModel.select(value)
    }
}

private fun <T> node(): Node = ToolBar(
    Button(DeskBot.resources.getString("action.refresh")).apply {
        id = "actionButton"
    },
    FxChoiceBox<T>().apply {
        id = "text"
        isDisable = true
        maxWidth = Double.MAX_VALUE
        HBox.setHgrow(this, Priority.ALWAYS)
    }).apply {
    orientation = Orientation.HORIZONTAL
    background = Background.EMPTY
}
