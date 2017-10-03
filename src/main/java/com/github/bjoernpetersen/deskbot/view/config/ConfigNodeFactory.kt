package com.github.bjoernpetersen.deskbot.view.config

import com.github.bjoernpetersen.jmusicbot.config.Config
import com.github.bjoernpetersen.jmusicbot.config.ui.*
import javafx.scene.Node
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.control.CheckBox as FxCheckBox
import javafx.scene.control.ChoiceBox as FxChoiceBox

private fun <T : Config.Entry> TextField.registerListener(entry: T, node: UiNode<T, String?>) {
  this.textProperty().addListener({ _: Any, _: Any?, new: String? ->
    node.converter.set(entry, new?.trim())
  })
}

private fun <T : Config.Entry> createNode(entry: T, node: TextBox<T>): Node = TextField().apply {
  val conv = node.converter
  promptText = conv.getDefault(entry)
  text = conv.getWithoutDefault(entry)
  this.registerListener(entry, node)
}

private fun <T : Config.Entry> createNode(entry: T,
    node: PasswordBox<T>): Node = PasswordField().apply {
  text = node.converter.getWithoutDefault(entry)
  this.registerListener(entry, node)
}

private fun <T : Config.Entry> createNode(entry: T, node: NumberBox<T>): Node = TODO()

private fun <T : Config.Entry> createNode(entry: T, node: CheckBox<T>): Node = FxCheckBox().apply {
  isSelected = node.converter.getWithoutDefault(entry)
  selectedProperty().addListener({ _: Any, _: Any?, new: Boolean? ->
    node.converter.set(entry, new ?: false)
  })
}

private fun <T : Config.Entry> createNode(entry: T,
    node: ChoiceBox<T>): Node = FxChoiceBox<String>().apply {
  // TODO add refresh button
  node.refresh()?.forEach { items.add(it) }
  val current = node.converter.getWithoutDefault(entry) ?: node.converter.getDefault(entry)
  if(current != null) {
    selectionModel.select(current)
  }
  selectionModel.selectedItemProperty().addListener({ _: Any, _: Any?, new: String? ->
    node.converter.set(entry, new?.trim())
  })
}

fun createNode(entry: Config.Entry): Node = entry.ui.let {
  when (it) {
    is TextBox -> createNode(entry, it as TextBox<Config.Entry>)
    is PasswordBox -> createNode(entry, it as PasswordBox<Config.Entry>)
    is NumberBox -> createNode(entry, it as NumberBox<Config.Entry>)
    is CheckBox -> createNode(entry, it as CheckBox<Config.Entry>)
    is ChoiceBox -> createNode(entry, it as ChoiceBox<Config.Entry>)
    else -> throw IllegalStateException()
  }
}
