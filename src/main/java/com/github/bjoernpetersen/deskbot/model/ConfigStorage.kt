package com.github.bjoernpetersen.deskbot.model

import com.github.bjoernpetersen.jmusicbot.Loggable
import com.github.bjoernpetersen.jmusicbot.config.ConfigStorageAdapter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class ConfigStorage(private val configFile: File, private val secretsFile: File) : Loggable, ConfigStorageAdapter {

  private fun load(file: File): Map<String, String> {
    logFine("Loading config from file: " + file.name)
    if (!file.isFile) {
      return emptyMap()
    }

    return try {
      val properties = Properties()
      properties.load(FileInputStream(file))
      properties.entries.associateBy({ it.key.toString() }, { it.value.toString() })
    } catch (e: IOException) {
      logWarning(e, "Could not load config entries from file '%s'", file.name)
      emptyMap()
    }

  }

  private fun store(file: File, map: Map<String, String>) {
    logFine("Storing config in file: " + file.name)
    if (!file.exists()) {
      try {
        if (!file.createNewFile()) {
          throw IOException("Could not create file")
        }
      } catch (e: IOException) {
        logSevere(e, "Could not create config file '%s'", file.name)
        return
      }
    }

    val properties = Properties()
    properties.putAll(map)
    try {
      properties.store(FileOutputStream(file, false), null)
    } catch (e: IOException) {
      logSevere(e, "Could not store config entries in file '%s'", file.name)
    }
  }

  override fun loadPlaintext() = load(configFile)

  override fun storePlaintext(map: Map<String, String>) {
    store(configFile, map)
  }

  override fun loadSecrets() = load(secretsFile)

  override fun storeSecrets(map: Map<String, String>) {
    store(secretsFile, map)
  }
}
