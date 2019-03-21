package net.bjoernpetersen.deskbot.impl

import mu.KotlinLogging
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.util.FileStorage
import java.io.File
import java.io.IOException
import javax.inject.Inject

class FileStorageImpl @Inject private constructor(mainConfig: MainConfigEntries) : FileStorage {
    private val logger = KotlinLogging.logger { }

    private val root: File = mainConfig.storageDir.get()!!

    private fun getPluginRoot(plugin: Plugin): File {
        return File(root, plugin::class.qualifiedName)
    }

    private fun getTempRoot(plugin: Plugin): File {
        return File(getPluginRoot(plugin), TMP_DIR)
    }

    override fun forPlugin(plugin: Plugin, clean: Boolean): File {
        val dir = File(getPluginRoot(plugin), "main")
        if (dir.isDirectory && clean) {
            dir.walkBottomUp()
                .filter { it != dir }
                .forEach { it.delete() }
        } else if (!dir.exists()) {
            if (!dir.mkdirs()) {
                throw IOException("Could not create dir: ${dir.path}")
            }
        }
        // TODO make sure nobody created a file instead of a dir...
        return dir
    }

    override fun createTemporaryFile(plugin: Plugin, name: String, ext: String): File {
        return File.createTempFile(name, ext, getTempRoot(plugin))
    }

    override fun createTemporaryDirectory(plugin: Plugin, name: String): File {
        val root = getTempRoot(plugin)
        val dir = generateSequence { File(root, "$name-${randomString(8)}") }
            .first { !it.exists() }

        if (!dir.mkdirs()) {
            throw IOException("Could not create dir: ${dir.path}")
        }

        return dir
    }

    override fun close() {
        root.listFiles()
            .map { pluginRoot -> File(pluginRoot, TMP_DIR) }
            .filter { it.exists() }
            .forEach {
                val success = if (it.isFile) it.delete()
                else it.deleteRecursively()

                if (!success) {
                    logger.error { "Could not delete temporary item ${it.path}" }
                }
            }
    }

    private companion object {
        const val TMP_DIR = "tmp"
    }
}
