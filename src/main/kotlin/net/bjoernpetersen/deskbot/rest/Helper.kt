@file:Suppress("UNCHECKED_CAST")

package net.bjoernpetersen.deskbot.rest

import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Throws(NotFoundException::class)
fun PluginFinder.findProvider(id: String, classLoader: ClassLoader): Provider {
    val base = try {
        classLoader.loadClass(id).kotlin
    } catch (e: ClassNotFoundException) {
        throw NotFoundException()
    }

    if (!base.isSubclassOf(Provider::class)) {
        throw NotFoundException()
    }

    return this[base as KClass<out Provider>] ?: throw NotFoundException()
}

@Throws(NotFoundException::class)
fun PluginFinder.findSuggester(id: String, classLoader: ClassLoader): Suggester {
    val base = try {
        classLoader.loadClass(id).kotlin
    } catch (e: ClassNotFoundException) {
        throw NotFoundException()
    }

    if (!base.isSubclassOf(Suggester::class)) {
        throw NotFoundException()
    }

    return this[base as KClass<out Suggester>] ?: throw NotFoundException()
}
