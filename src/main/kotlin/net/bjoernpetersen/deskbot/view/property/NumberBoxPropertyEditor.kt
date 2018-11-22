package net.bjoernpetersen.deskbot.view.property

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.scene.control.TextField
import net.bjoernpetersen.deskbot.view.createObjectBinding
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.config.NumberBox
import org.controlsfx.property.editor.AbstractPropertyEditor

class NumberBoxPropertyEditor(
    item: ConfigEntryItem<Int>,
    entry: Config.SerializedEntry<Int>,
    numberBox: NumberBox
) : AbstractPropertyEditor<Int, TextField>(item, TextField()) {

    init {
        editor.promptText = entry.default?.toString()
        observableValue.addListener { _, _, number ->
            Platform.runLater {
                number?.let {
                    when {
                        it < numberBox.min -> value = numberBox.min
                        it > numberBox.max -> value = numberBox.max
                    }
                }
            }
        }
    }

    override fun getObservableValue(): ObservableValue<Int> =
        createObjectBinding(editor.textProperty()) {
            editor.text?.toIntOrNull()
        }

    override fun setValue(value: Int?) {
        editor.text = value?.toString()
    }
}
