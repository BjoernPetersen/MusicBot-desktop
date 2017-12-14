package com.github.bjoernpetersen.deskbot.view

import com.github.bjoernpetersen.jmusicbot.platform.ContextSupplier
import com.github.bjoernpetersen.jmusicbot.platform.HostServices
import tornadofx.*
import java.net.URL

class JavaFxHostServices : HostServices {

  override fun openBrowser(url: URL) {
    val configWindow = find(ConfigWindow::class)
    configWindow.hostServices.showDocument(url.toExternalForm())
  }

  @Throws(IllegalStateException::class)
  override fun contextSupplier(): ContextSupplier {
    throw IllegalStateException("Context not available on Desktop systems")
  }
}
