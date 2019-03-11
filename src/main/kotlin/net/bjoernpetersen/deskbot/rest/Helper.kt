@file:Suppress("UNCHECKED_CAST")

package net.bjoernpetersen.deskbot.rest

import net.bjoernpetersen.musicbot.spi.plugin.PluginLookup
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester

@Throws(NotFoundException::class)
fun PluginLookup.findProvider(id: String): Provider {
    return lookup(id) ?: throw NotFoundException()
}

@Throws(NotFoundException::class)
fun PluginLookup.findSuggester(id: String): Suggester {
    return lookup(id) ?: throw NotFoundException()
}
