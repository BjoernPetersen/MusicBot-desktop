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
import javafx.stage.FileChooser
import net.bjoernpetersen.deskbot.view.DeskBot
import net.bjoernpetersen.deskbot.view.property
import net.bjoernpetersen.musicbot.api.config.PathChooser
import org.controlsfx.property.editor.PropertyEditor
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

class PathChooserPropertyEditor(
    private val item: ConfigEntryItem<Path>,
    fileChooser: PathChooser
) : PropertyEditor<Path>, Validatable<Path> {

    private val fileProperty: ObjectProperty<Path> = SimpleObjectProperty(item.entry.get())
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
                    initialDirectory = file?.findDir()?.toFile() ?: File(".")
                }
                chooser.showDialog(null)
            } else {
                val chooser = FileChooser().apply {
                    val file = file
                    initialDirectory = file?.findDir()?.toFile() ?: File(".")
                    if (file?.let { Files.isRegularFile(it) } == true) {
                        initialFileName = file.fileName.toString()
                    }
                }
                if (fileChooser.isOpen) chooser.showOpenDialog(null)
                else chooser.showSaveDialog(null)
            }

            if (file != null) {
                this.file = file.toPath()
            }
        }
    }

    private fun Path.findDir(): Path? {
        if (Files.isDirectory(this)) return this
        val parent = parent
        return parent?.findDir()
    }

    override fun setValue(value: Path?) {
        node.textField.text = value?.toString()
    }

    override fun getEditor(): Node = node
    override fun getValue(): Path? = item.entry.get()
}

private val Node.button: Button
    get() = (this as ToolBar).items[0] as Button

private val Node.textField: TextField
    get() = (this as ToolBar).items[1] as TextField

private val PathChooser.label: String
    get() = DeskBot.resources.getString("choose.${if (isDirectory) "dir" else "file"}")

private fun node(fileChooser: PathChooser): Node = ToolBar(
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
