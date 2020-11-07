package net.bjoernpetersen.deskbot.impl

import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.player.SongEntry
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.api.plugin.management.findDependencies
import net.bjoernpetersen.musicbot.spi.player.SongPlayedNotifier
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import javax.inject.Inject
import kotlin.reflect.full.isSubclassOf

class SongPlayedNotifierImpl @Inject private constructor(
    finder: PluginFinder
) : SongPlayedNotifier {

    private val suggestersByProviderId: Multimap<String, Suggester> = MultimapBuilder
        .hashKeys()
        .arrayListValues()
        .build()

    init {
        finder.suggesters.forEach { suggester ->
            suggester
                .findDependencies()
                .filter { it.isSubclassOf(Provider::class) }
                .map { it.qualifiedName!! }
                .forEach { suggestersByProviderId.put(it, suggester) }
        }
    }

    override suspend fun notifyPlayed(songEntry: SongEntry) {
        val permissions = songEntry.user?.permissions
        val suggesters = suggestersByProviderId[songEntry.song.provider.id]
        if (permissions == null || Permission.ALTER_SUGGESTIONS in permissions) {
            suggesters.forEach {
                it.notifyPlayed(songEntry)
            }
        } else {
            suggesters.forEach {
                it.removeSuggestion(songEntry.song)
            }
        }
    }
}

class SongPlayedNotifierModule : AbstractModule() {
    override fun configure() {
        bind(SongPlayedNotifier::class.java)
            .to(SongPlayedNotifierImpl::class.java)
            .`in`(Scopes.SINGLETON)
    }
}
