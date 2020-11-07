package net.bjoernpetersen.deskbot.view.property

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Control
import javafx.scene.control.TextField
import javafx.scene.control.ToolBar
import javafx.scene.layout.Background
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.config.ActionButton
import org.controlsfx.property.editor.PropertyEditor

class ActionButtonPropertyEditor<T : Any>(
    private val item: ConfigEntryItem<T>,
    private val actionButton: ActionButton<T>
) : PropertyEditor<T>, Validatable<T> {

    private val logger = KotlinLogging.logger { }

    private val node = node(actionButton)
    private val observableValue: ObjectProperty<T> = SimpleObjectProperty(item.entry.get())

    override val control: Control
        get() = editor.textField

    init {
        val button = editor.button
        button.setOnAction {
            button.isDisable = true
            CoroutineScope(Dispatchers.Default).launch {
                val success = try {
                    actionButton.action(item.entry)
                } catch (e: Throwable) {
                    logger.error(e) { "Action button failed" }
                    false
                }

                // TODO react to failure
                logger.debug { "Action successful: $success" }

                withContext(Dispatchers.Main) {
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
    }
).apply {
    orientation = Orientation.HORIZONTAL
    background = Background.EMPTY
}
