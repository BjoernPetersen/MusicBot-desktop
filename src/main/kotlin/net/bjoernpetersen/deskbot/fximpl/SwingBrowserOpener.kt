package net.bjoernpetersen.deskbot.fximpl

import net.bjoernpetersen.musicbot.spi.util.BrowserOpener
import java.awt.Desktop
import java.net.URL
import javax.swing.SwingUtilities

class SwingBrowserOpener : BrowserOpener {
    // Since JavaFX doesn't provide its hostServices on half the supported OSes, we're using Swing,
    // because apparently it's 1997.
    override fun openDocument(url: URL) {
        SwingUtilities.invokeLater {
            Desktop.getDesktop().browse(url.toURI())
        }
    }
}
