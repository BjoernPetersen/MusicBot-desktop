package net.bjoernpetersen.deskbot.impl

import java.io.File
import javax.inject.Inject
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.util.FileStorage

class FileStorageImpl @Inject private constructor(mainConfig: MainConfigEntries) : FileStorage {
    private val root: File = mainConfig.storageDir.get()!!.toFile()
    override fun forPlugin(plugin: Plugin, clean: Boolean): File {
        val dir = File(root, plugin::class.qualifiedName)
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
