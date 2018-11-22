package net.bjoernpetersen.deskbot.view.property

import javafx.beans.value.ObservableValue
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import net.bjoernpetersen.musicbot.api.config.Config
import org.controlsfx.property.editor.AbstractPropertyEditor

class TextFieldPropertyEditor(
    item: ConfigEntryItem<String>,
    entry: Config.StringEntry,
    textField: TextField = TextField()
) : AbstractPropertyEditor<String, TextField>(item, textField) {

    init {
        if (textField !is PasswordField)
            textField.promptText = entry.default
    }

    override fun setValue(value: String?) {
        editor.text = value
    }

    override fun getObservableValue(): ObservableValue<String> {
        return editor.textProperty()
    }
}
