package com.github.bjoernpetersen.deskbot.view

import javafx.application.Platform
import tornadofx.*

/**
 * Runs the specified op on the UI thread. If the current thread is the UI thread, it will be executed immediately.
 *
 * @return true, if the op was executed immediately
 */
fun runOnUi(op: () -> Unit): Boolean = if (Platform.isFxApplicationThread()) {
  op()
  true
} else {
  runLater(op)
  false
}
