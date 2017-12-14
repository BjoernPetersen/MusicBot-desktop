package com.github.bjoernpetersen.deskbot.view

import com.github.bjoernpetersen.jmusicbot.*
import javafx.concurrent.Task
import javafx.scene.control.Alert
import javafx.scene.control.ButtonBase
import javafx.scene.control.ButtonType
import javafx.scene.control.DialogPane
import tornadofx.*
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*
import java.util.logging.Logger
import kotlin.reflect.KClass

class LoadingWindow : Fragment("DeskBot loading...") {
  val builder: MusicBot.Builder by param()
  private var warningCollector: WarningCollector by singleAssign()
  private val build: Task<MusicBot>

  override val root = DialogPane().apply {
    headerText = "init..." //TODO
    buttonTypes.clear()
    buttonTypes.add(ButtonType.CANCEL)
    val cancel = lookupButton(ButtonType.CANCEL) as ButtonBase
    cancel.setOnAction { cancel() }
  }

  override fun onDock() {
    super.onDock()
    primaryStage.sizeToScene()
  }

  private fun cancel() {
    build.cancel()
  }

  private fun showError(cause: Throwable) {
    val writer = StringWriter()
    val printer = PrintWriter(writer)
    cause.printStackTrace(printer)
    printer.close()
    Alert(Alert.AlertType.ERROR).apply {
      headerText = "An error occurred during initialization: ${cause.message}"
      contentText = writer.toString()
      isResizable = true
    }.showAndWait()
  }

  private fun onFail(cause: Throwable) {
    showError(cause)
    showWarnings(warningCollector.nonEmptyWarnings)
    onFail()
  }

  private fun onFail() {
    primaryStage.onHiding = null
    find<ConfigController>().reload()
    replaceWith<ConfigWindow>()
  }

  private fun showWarnings(warnings: List<Warnings>) {
    if (!warnings.isEmpty()) {
      alert(Alert.AlertType.WARNING,
          "Warnings occurred during initialization",
          warnings.asSequence()
              .map { it.toWarningSection() }
              .joinToString("\n-------------------------------------\n")) {
        isResizable = true
      }
    }
  }

  private fun onDone(warnings: List<Warnings>) {
    showWarnings(warnings)
    primaryStage.onHiding = null
    replaceWith(find<PlayerWindow>(mapOf(PlayerWindow::bot to build.get())))
  }

  init {
    warningCollector = WarningCollector(root)
    builder.initStateWriter(warningCollector)
    build = runAsync(daemon = true) {
      // TODO I am NOT happy with making this a daemon
      builder.build()
    }
    build.setOnSucceeded { runOnUi { onDone(warningCollector.nonEmptyWarnings) } }
    build.setOnCancelled { runOnUi { onFail() } }
    build.setOnFailed { runOnUi { onFail(it.source.exception) } }

    primaryStage.setOnHiding { cancel() }
  }
}

private class WarningCollector(private val dialogPane: DialogPane) :
    InitStateWriter, Loggable {

  val nonEmptyWarnings: List<Warnings>
    get() = warnings.map { it.value }.filter { !it.warnings.isEmpty() }
  private val _logger = createLogger()
  private val warnings = HashMap<KClass<out Plugin>, Warnings>()
  private var current: Warnings? = null
    set(value) {
      val old = field
      if (old != null) {
        warnings[old.plugin::class] = old
      }
      field = value
    }

  override fun getLogger(): Logger = _logger

  override fun begin(plugin: Plugin) {
    current = Warnings(plugin)
    runOnUi {
      dialogPane.headerText = "Initializing ${plugin.qualifiedReadableName()}..."
      dialogPane.contentText = ""
    }
  }

  override fun state(state: String) {
    logFiner("State from plugin ${current?.plugin?.qualifiedReadableName()}: $state")
    runOnUi {
      dialogPane.contentText = state
    }
  }

  override fun warning(warning: String) {
    logInfo("Warning from plugin ${current?.plugin?.qualifiedReadableName()}: $warning")
    current?.warnings?.add(warning)
    runOnUi {
      dialogPane.contentText = warning
    }
  }

  override fun close() {
    current = null
  }

  fun clear() {
    warnings.clear()
  }
}

private class Warnings(val plugin: Plugin) {
  val warnings: MutableList<String> = LinkedList()
  fun toWarningSection(): String = "${plugin.qualifiedReadableName()}:\n${warnings.joinToString("\n\n")}"
}
