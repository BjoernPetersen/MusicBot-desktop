@file:JvmName("Util")

package com.github.bjoernpetersen.deskbot.api.swag.api.impl

import com.github.bjoernpetersen.jmusicbot.IdPlugin
import com.github.bjoernpetersen.jmusicbot.Song
import com.github.bjoernpetersen.jmusicbot.playback.PlayerState
import com.github.bjoernpetersen.jmusicbot.playback.QueueEntry
import com.github.bjoernpetersen.jmusicbot.playback.SongEntry
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException
import com.github.bjoernpetersen.jmusicbot.provider.Provider
import com.github.bjoernpetersen.jmusicbot.provider.ProviderManager
import com.github.bjoernpetersen.jmusicbot.provider.Suggester
import java.util.stream.Stream
import kotlin.streams.toList

typealias ModelSong = com.github.bjoernpetersen.deskbot.api.swag.model.Song
typealias ModelQueueEntry = com.github.bjoernpetersen.deskbot.api.swag.model.QueueEntry
typealias ModelSongEntry = com.github.bjoernpetersen.deskbot.api.swag.model.SongEntry
typealias ModelPlayerState = com.github.bjoernpetersen.deskbot.api.swag.model.PlayerState
typealias ModelPlayerStateEnum = com.github.bjoernpetersen.deskbot.api.swag.model.PlayerState.StateEnum
typealias ModelNamedPlugin = com.github.bjoernpetersen.deskbot.api.swag.model.NamedPlugin


fun Song.convert(): ModelSong {
    val result = ModelSong()
    result.id = this.id
    result.provider = this.provider.convert()
    result.title = this.title
    result.description = this.description
    result.duration = this.duration
    result.albumArtUrl = this.albumArtUrl.orElse(null)
    return result
}

fun IdPlugin.convert(): ModelNamedPlugin {
    val result = ModelNamedPlugin()
    result.id = this.id
    result.name = this.readableName
    return result
}

fun Stream<out IdPlugin>.convert(): List<ModelNamedPlugin> = this.map { it.convert() }.toList()

fun QueueEntry.convert(): ModelQueueEntry {
    val queueEntry = ModelQueueEntry()
    queueEntry.userName = this.user.name
    queueEntry.song = this.song.convert()
    return queueEntry
}

fun SongEntry.convert(): ModelSongEntry {
    val song = this.song.convert()
    val user = this.user
    val userName = user?.name
    val queueEntry = ModelSongEntry()
    queueEntry.userName = userName
    queueEntry.song = song
    return queueEntry
}

fun List<QueueEntry>.convert(): List<ModelQueueEntry> = this.map { it.convert() }

fun PlayerState.convert(): ModelPlayerState {
    val result = ModelPlayerState()
    result.state = ModelPlayerStateEnum.valueOf(this.state.name)
    result.songEntry = this.entry
            .map { it.convert() }
            .orElse(null)
    return result
}

fun lookupProvider(manager: ProviderManager, providerId: String): Provider? = manager.getProvider(providerId)

fun lookupSuggester(manager: ProviderManager, suggesterId: String): Suggester? = manager.getSuggester(suggesterId)

fun lookupSong(manager: ProviderManager, songId: String, providerId: String): Song? =
        try {
            lookupProvider(manager, providerId)?.lookup(songId)
        } catch (e: NoSuchSongException) {
            null
        }
