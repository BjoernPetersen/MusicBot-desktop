package net.bjoernpetersen.deskbot.fximpl

import javafx.application.HostServices
import net.bjoernpetersen.musicbot.spi.util.BrowserOpener
import java.net.URL

class JavaFxBrowserOpener(private val hostServices: HostServices) : BrowserOpener {
    override fun openDocument(url: URL) {
        hostServices.showDocument(url.toExternalForm())
    }
}
