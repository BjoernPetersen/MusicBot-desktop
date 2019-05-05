package net.bjoernpetersen.deskbot.view.property

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.scene.control.Control
import javafx.scene.control.TextField
import net.bjoernpetersen.deskbot.view.createObjectBinding
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.config.NumberBox
import org.controlsfx.property.editor.AbstractPropertyEditor

class NumberBoxPropertyEditor(
    item: ConfigEntryItem<Int>,
    entry: Config.SerializedEntry<Int>,
    private val numberBox: NumberBox
) : AbstractPropertyEditor<Int, TextField>(item, TextField()), Validatable<Int> {

    private var observable: ObservableValue<Int>? = null
        set(value) {
            field = value
            value?.addListener { _, _, number ->
                Platform.runLater {
                    number?.let {
                        when {
                            it < numberBox.min -> this.value = numberBox.min
                            it > numberBox.max -> this.value = numberBox.max
                        }
                    }
                }
            }
        }

    init {
        editor.promptText = entry.default?.toString()
    }

    override val control: Control
        get() = editor

    override fun getObservableValue(): ObservableValue<Int> = if (observable == null) {
        observable = createObjectBinding(editor.textProperty()) {
            editor.text?.toIntOrNull()
        }
        observable!!
    } else observable!!

    override fun setValue(value: Int?) {
        editor.text = value?.toString()
    }
}
