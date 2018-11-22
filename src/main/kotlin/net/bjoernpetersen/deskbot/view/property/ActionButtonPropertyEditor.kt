package net.bjoernpetersen.deskbot.view.property

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.control.ToolBar
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.config.ActionButton
import org.controlsfx.property.editor.PropertyEditor
import kotlin.concurrent.thread

class ActionButtonPropertyEditor<T : Any>(
    private val item: ConfigEntryItem<T>,
    private val actionButton: ActionButton<T>
) : PropertyEditor<T> {

    private val logger = KotlinLogging.logger { }

    private val node = node(actionButton)
    private val observableValue: ObjectProperty<T> = SimpleObjectProperty(item.entry.get())

    init {
        val button = editor.button
        button.setOnAction {
            button.isDisable = true
            thread(name = "Action ${item.entry.key}", isDaemon = true) {
                val success = try {
                    actionButton.action()
                } catch (e: Throwable) {
                    logger.error(e) { "Action button failed" }
                    false
                }

                // TODO react to failure
                logger.debug { "Action successful: $success" }

                Platform.runLater {
                    button.isDisable = false
                    observableValue.value = item.entry.get()
                }
            }
        }
        observableValue.addListener { _, _, value ->
            setValue(value)
        }
    }

    override fun getEditor(): Node {
        return node
    }

    override fun getValue(): T? {
        return item.entry.get()
    }

    override fun setValue(value: T?) {
        editor.textField.text = value?.let { actionButton.descriptor(it) }
    }
}

private val Node.button: Button
    get() = (this as ToolBar).items[0] as Button

private val Node.textField: TextField
    get() = (this as ToolBar).items[1] as TextField

private fun <T> node(actionButton: ActionButton<T>): Node = ToolBar(
    Button(actionButton.label).apply {
        id = "actionButton"
    },
    TextField().apply {
        id = "text"
        isEditable = false
        isDisable = true
        prefWidth = 20.0
        HBox.setHgrow(this, Priority.ALWAYS)
    }).apply {
    orientation = Orientation.HORIZONTAL
    background = Background.EMPTY
}

