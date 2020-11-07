package net.bjoernpetersen.deskbot.view.property

import javafx.scene.control.PasswordField
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.config.ActionButton
import net.bjoernpetersen.musicbot.api.config.CheckBox
import net.bjoernpetersen.musicbot.api.config.ChoiceBox
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.config.FileChooser
import net.bjoernpetersen.musicbot.api.config.NumberBox
import net.bjoernpetersen.musicbot.api.config.PasswordBox
import net.bjoernpetersen.musicbot.api.config.PathChooser
import net.bjoernpetersen.musicbot.api.config.TextBox
import org.controlsfx.control.PropertySheet
import org.controlsfx.property.editor.DefaultPropertyEditorFactory
import org.controlsfx.property.editor.PropertyEditor
import org.controlsfx.validation.ValidationSupport
import java.io.File
import java.nio.file.Path

class ConfigEntryEditorFactory : DefaultPropertyEditorFactory() {
    private val logger = KotlinLogging.logger { }
    override fun call(item: PropertySheet.Item): PropertyEditor<*>? {

        return if (item is ConfigEntryItem<*>) call(item)
        else super.call(item)?.let {
            logger.debug { "Default could handle it" }
            it
        }
    }

    private fun call(item: ConfigEntryItem<*>): PropertyEditor<*>? {
        val entry = item.entry
        val uiNode = entry.uiNode ?: return null
        @Suppress("UNCHECKED_CAST")
        return when (uiNode) {
            is TextBox -> TextFieldPropertyEditor(
                item as ConfigEntryItem<String>,
                item.entry as Config.StringEntry
            )
            is PasswordBox -> TextFieldPropertyEditor(
                item as ConfigEntryItem<String>,
                item.entry as Config.StringEntry, PasswordField()
            )
            is CheckBox -> ValidatableWrapper(super.call(item))
            is NumberBox -> NumberBoxPropertyEditor(
                item as ConfigEntryItem<Int>,
                item.entry as Config.SerializedEntry<Int>,
                uiNode
            )
            is ActionButton<*> -> ActionButtonPropertyEditor(
                item as ConfigEntryItem<Any>,
                uiNode as ActionButton<Any>
            )
            is FileChooser -> FileChooserPropertyEditor(
                item as ConfigEntryItem<File>,
                uiNode
            )
            is PathChooser -> PathChooserPropertyEditor(
                item as ConfigEntryItem<Path>,
                uiNode
            )
            is ChoiceBox<*> -> ChoiceBoxPropertyEditor(
                item as ConfigEntryItem<Any>,
                uiNode as ChoiceBox<Any>
            )
            else -> {
                logger.warn { "Not implemented: ${uiNode::class.simpleName}" }
                null
            }
        }?.also {
            val validation = ValidationSupport()
            validation.registerValidator(it.control, false, ValidatorAdapter(entry))
        }
    }
}
