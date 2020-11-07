package net.bjoernpetersen.deskbot.impl

import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.config.ConfigScope
import net.bjoernpetersen.musicbot.api.config.GenericConfigScope
import net.bjoernpetersen.musicbot.api.config.MainConfigScope
import net.bjoernpetersen.musicbot.api.config.PluginConfigScope
import net.bjoernpetersen.musicbot.spi.config.ConfigStorageAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Properties

class FileConfigStorage(private val configDir: File) : ConfigStorageAdapter {
    private val logger = KotlinLogging.logger { }

    override fun load(scope: ConfigScope): Map<String, String> {
        return load(scope.toFile())
    }

    override fun store(scope: ConfigScope, config: Map<String, String>) {
        return store(scope.toFile(), config)
    }

    private fun ConfigScope.toFile(): File = File(
        configDir,
        when (this) {
            is GenericConfigScope, MainConfigScope -> "${toString()}.properties"
            is PluginConfigScope -> "plugins/${toString()}.properties"
        }
    )

    private fun load(file: File): Map<String, String> {
        logger.debug { "Loading config from file: " + file.path }
        if (!file.isFile) {
            return emptyMap()
        }

        return try {
            val properties = Properties()
            properties.load(FileInputStream(file))
            properties.entries.associateBy({ it.key.toString() }, { it.value.toString() })
        } catch (e: IOException) {
            logger.warn(e) { "Could not load config entries from file '${file.name}'" }
            emptyMap()
        }
    }

    private fun store(file: File, map: Map<String, String>) {
        logger.debug { "Storing config in file: " + file.name }

        val parent = file.parentFile!!
        if (!parent.exists() && !parent.mkdirs()) {
            logger.error { "Could not create config dir ${parent.path}" }
            return
        }

        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    throw IOException("Could not create file")
                }
            } catch (e: IOException) {
                logger.error(e) { "Could not create config file '${file.name}'" }
                return
            }
        }

        val properties = Properties()
        properties.putAll(map)
        try {
            properties.store(FileOutputStream(file, false), null)
        } catch (e: IOException) {
            logger.error(e) { "Could not store config entries in file '${file.name}'" }
        }
    }
}
