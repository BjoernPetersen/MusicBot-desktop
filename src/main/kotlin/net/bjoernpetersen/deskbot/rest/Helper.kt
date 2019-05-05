@file:Suppress("UNCHECKED_CAST")

package net.bjoernpetersen.deskbot.rest

import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.PluginLookup
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester

@Throws(NotFoundException::class)
fun PluginLookup.findProvider(id: String): Provider {
    return lookup<Plugin>(id) as? Provider ?: throw NotFoundException()
}

@Throws(NotFoundException::class)
fun PluginLookup.findSuggester(id: String): Suggester {
    return lookup<Plugin>(id) as? Suggester ?: throw NotFoundException()
}
