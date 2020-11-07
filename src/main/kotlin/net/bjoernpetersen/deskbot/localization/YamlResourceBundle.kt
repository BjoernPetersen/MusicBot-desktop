package net.bjoernpetersen.deskbot.localization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import java.io.InputStream
import java.util.Collections
import java.util.Enumeration
import java.util.HashMap
import java.util.Locale
import java.util.ResourceBundle

class YamlResourceBundle(inputStream: InputStream) : ResourceBundle() {
    @Suppress("unused")
    private val logger = KotlinLogging.logger {}

    private val values: Map<String, Any> = mapper
        .readValue<Map<String, Any>>(inputStream).flatten()

    @Suppress("UNCHECKED_CAST")
    private fun Map<String, Any>.flatten(
        target: MutableMap<String, Any> = HashMap(size * 4),
        base: String = ""
    ): Map<String, Any> {
        this.forEach { key, value ->
            val newKey = "$base$key"
            when (value) {
                is Map<*, *> -> (value as Map<String, Any>).flatten(target, "$newKey.")
                is List<*> -> target[newKey] = value.map {
                    if (it is String) it else throw IllegalArgumentException()
                }
                else -> target[newKey] = value.toString()
            }
        }
        return target
    }

    override fun getKeys(): Enumeration<String> {
        return Collections.enumeration(values.keys)
    }

    override fun handleGetObject(key: String?): Any? = values[key]

    object Control : ResourceBundle.Control() {
        override fun getFormats(baseName: String?) = listOf("yml", "yaml")
        override fun newBundle(
            baseName: String,
            locale: Locale,
            format: String,
            loader: ClassLoader,
            reload: Boolean
        ): ResourceBundle {
            val bundleName = toBundleName(baseName, locale)
            val resourceName = toResourceName(bundleName, format)

            val inputStream = if (reload) {
                val url = loader.getResource(resourceName)
                url.openConnection()
                    .apply { useCaches = false }
                    .getInputStream()
            } else loader.getResourceAsStream(resourceName)

            return YamlResourceBundle(inputStream)
        }
    }

    private companion object {
        val mapper = ObjectMapper(YAMLFactory()).apply {
            registerModule(KotlinModule())
        }
    }
}
