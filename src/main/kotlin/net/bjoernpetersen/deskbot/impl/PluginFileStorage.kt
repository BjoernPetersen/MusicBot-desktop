package net.bjoernpetersen.deskbot.impl

import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.util.FileStorage
import java.io.File

class FileStorageImpl : FileStorage {
    // TODO should be configurable
    private val parent = File("storage")

    override fun forPlugin(plugin: Plugin, clean: Boolean): File {
        val dir = File(parent, plugin::class.qualifiedName)
        if (dir.isDirectory && clean) {
            dir.walkBottomUp()
                .filter { it != dir }
                .forEach { it.delete() }
        } else if (!dir.exists()) {
            // TODO check for success
            dir.mkdirs()
        }
        // TODO make sure nobody created a file instead of a dir...
        return dir
    }
}
