package com.github.bjoernpetersen.deskbot.view.config

import com.github.bjoernpetersen.jmusicbot.config.Config
import com.github.bjoernpetersen.jmusicbot.config.ui.*
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.layout.HBox
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Window
import java.io.File
import javafx.scene.control.CheckBox as FxCheckBox
import javafx.scene.control.ChoiceBox as FxChoiceBox

private fun TextField.registerListener(entry: Config.StringEntry,
    node: UiNode<Config.StringEntry, String?, String?>) {
  this.textProperty().addListener({ _: Any, _: Any?, new: String? ->
    node.converter.set(entry, new?.trim())
  })
}

private fun createNode(entry: Config.StringEntry, node: TextBox): Node = TextField().apply {
  val conv = node.converter
  promptText = conv.getDefault(entry)
  text = conv.getWithoutDefault(entry)
  this.registerListener(entry, node)
}

private fun createNode(entry: Config.StringEntry, node: PasswordBox): Node = PasswordField().apply {
  text = node.converter.getWithoutDefault(entry)
  this.registerListener(entry, node)
}

private fun createNode(entry: Config.StringEntry, node: NumberBox): Node = TODO()

private fun createNode(entry: Config.BooleanEntry, node: CheckBox): Node = FxCheckBox().apply {
  isSelected = node.converter.getWithoutDefault(entry)
  selectedProperty().addListener({ _: Any, _: Any?, new: Boolean? ->
    node.converter.set(entry, new ?: false)
  })
}

private fun <I : Any, T : Choice<I>> createNode(entry: Config.StringEntry,
    node: ChoiceBox<I, T>): Node {
  var items: List<T?> = emptyList()
  val choiceBox = FxChoiceBox<String>().apply {
    isDisable = true
    val current = node.converter.getWithDefault(entry)
    if (current != null) {
      this.items.add("(Is set)")
      selectionModel.select(0)
    }
    selectionModel.selectedIndexProperty().addListener({ _: Any, _: Any?, new: Number? ->
      if (new!!.toInt() > -1) node.converter.set(
          entry,
          items[new.toInt()]?.id
      )
    })
  }
  val refresh: () -> Unit = {
    val new: List<T?>? = node.refresh()
    if (new != null) {
      items = new.toMutableList().apply { if (entry.defaultValue == null || node.lazy) add(null) }

      with(choiceBox.items) {
        clear();
        items.map { it?.displayName ?: "" }.forEach { add(it) }
      }

      val currentId = node.converter.getWithDefault(entry)
      val current = items.firstOrNull { it?.id == currentId }
          ?: if (entry.defaultValue != null) items.firstOrNull { it?.id == entry.defaultValue } else null
      choiceBox.selectionModel.select(current?.displayName ?: "")
    }
    choiceBox.isDisable = false
  }

  val box = HBox()
  box.children.add(choiceBox)
  box.children.add(Button().apply {
    text = "Refresh"
    onAction = EventHandler { refresh() }
  })
  if (!node.lazy) refresh()
  return box
}

private fun askForFolder(window: () -> Window, initial: String?): File? {
  val chooser = DirectoryChooser()
  chooser.title = "Please select a folder"

  if (initial != null) {
    val file = File(initial)
    if (file.isDirectory) {
      chooser.initialDirectory = file
    }
  }

  return chooser.showDialog(window())
}

private fun askForFile(window: () -> Window, initial: String?): File? {
  val chooser = FileChooser()
  chooser.title = "Please select a file"

  if (initial != null) {
    val file = File(initial)
    val parent = file.parentFile
    if (file.isFile && parent != null && parent.isDirectory) {
      chooser.initialDirectory = parent
      chooser.initialFileName = file.name
    }
  }

  return chooser.showOpenDialog(window())
}

private fun askForPath(window: () -> Window, isFolder: Boolean,
    initial: String?): File? = if (isFolder)
  askForFolder(window, initial) else askForFile(window, initial)

private fun createNode(window: () -> Window, entry: Config.StringEntry,
    node: FileChooserButton): Node = HBox().apply {
  val currentField = TextField().apply {
    text = node.converter.getWithDefault(entry)
    isEditable = false
  }
  children.add(currentField)
  children.add(Button().apply {
    text = "Choose " + if (node.isFolder) "folder" else "file"
    onAction = EventHandler {
      val chosenFile = askForPath(window, node.isFolder, currentField.text)
      chosenFile?.let { node.converter.set(entry, it.path) }
      currentField.text = node.converter.getWithDefault(entry)
    }
  })
}

private fun createNode(node: ActionButton): Node = Button().apply {
  text = node.text
  onAction = EventHandler {
    node.action()
  }
}

fun createNode(window: () -> Window, entry: Config.Entry): Node = entry.ui.let {
  when (it) {
    is TextBox -> createNode(entry as Config.StringEntry, it)
    is PasswordBox -> createNode(entry as Config.StringEntry, it)
    is CheckBox -> createNode(entry as Config.BooleanEntry, it)
    is NumberBox -> createNode(entry as Config.StringEntry, it)
    is ChoiceBox<*, *> -> createNode(entry as Config.StringEntry, it as ChoiceBox<Any, Choice<Any>>)
    is FileChooserButton -> createNode(window, entry as Config.StringEntry, it)
    is ActionButton -> createNode(it)
    else -> throw IllegalStateException()
  }
}
