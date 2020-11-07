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
import javafx.stage.DirectoryChooser
import net.bjoernpetersen.deskbot.view.DeskBot
import net.bjoernpetersen.deskbot.view.property
import net.bjoernpetersen.musicbot.api.config.FileChooser
import org.controlsfx.property.editor.PropertyEditor
import java.io.File

private typealias FxFileChooser = javafx.stage.FileChooser

class FileChooserPropertyEditor(
    private val item: ConfigEntryItem<File>,
    fileChooser: FileChooser
) : PropertyEditor<File>, Validatable<File> {

    private val fileProperty: ObjectProperty<File> = SimpleObjectProperty(item.entry.get())
    private var file by property(fileProperty)
    private val node = node(fileChooser)

    override val control: Control
        get() = node.button

    init {
        fileProperty.addListener { _, _, file ->
            item.value = file
            value = file
        }
        node.button.setOnAction {
            val file = if (fileChooser.isDirectory) {
                val chooser = DirectoryChooser().apply {
                    val file = file
                    initialDirectory = file?.findDir() ?: File(".")
                }
                chooser.showDialog(null)
            } else {
                val chooser = FxFileChooser().apply {
                    val file = file
                    initialDirectory = file?.findDir() ?: File(".")
                    if (file?.isFile == true) initialFileName = file.name
                }
                if (fileChooser.isOpen) chooser.showOpenDialog(null)
                else chooser.showSaveDialog(null)
            }

            if (file != null) {
                this.file = file
            }
        }
    }

    private fun File.findDir(): File? {
        if (isDirectory) return this
        val parent = parentFile
        return parent?.findDir()
    }

    override fun setValue(value: File?) {
        node.textField.text = value?.path
    }

    override fun getEditor(): Node = node
    override fun getValue(): File? = item.entry.get()
}

private val Node.button: Button
    get() = (this as ToolBar).items[0] as Button

private val Node.textField: TextField
    get() = (this as ToolBar).items[1] as TextField

private val FileChooser.label: String
    get() = DeskBot.resources.getString("choose.${if (isDirectory) "dir" else "file"}")

private fun node(fileChooser: FileChooser): Node = ToolBar(
    Button(fileChooser.label).apply {
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
