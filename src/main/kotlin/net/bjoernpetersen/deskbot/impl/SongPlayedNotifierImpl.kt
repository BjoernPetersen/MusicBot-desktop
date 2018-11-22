package net.bjoernpetersen.deskbot.impl

import com.google.inject.AbstractModule
import com.google.inject.Scopes
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.player.SongEntry
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.player.SongPlayedNotifier
import javax.inject.Inject

class SongPlayedNotifierImpl @Inject constructor(
    private val finder: PluginFinder) : SongPlayedNotifier {

    override fun notifyPlayed(songEntry: SongEntry) {
        val permissions = songEntry.user?.permissions ?: return
        if (Permission.ALTER_SUGGESTIONS !in permissions) {
            return
        }
        finder.suggesters.forEach {
            it.notifyPlayed(songEntry)
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
