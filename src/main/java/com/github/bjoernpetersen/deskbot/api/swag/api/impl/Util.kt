@file:JvmName("Util")

package com.github.bjoernpetersen.deskbot.api.swag.api.impl

import com.github.bjoernpetersen.jmusicbot.ProviderManager
import com.github.bjoernpetersen.jmusicbot.Song
import com.github.bjoernpetersen.jmusicbot.playback.PlayerState
import com.github.bjoernpetersen.jmusicbot.playback.QueueEntry
import com.github.bjoernpetersen.jmusicbot.playback.SongEntry
import com.github.bjoernpetersen.jmusicbot.provider.NoSuchSongException
import com.github.bjoernpetersen.jmusicbot.provider.Provider
import com.github.bjoernpetersen.jmusicbot.provider.Suggester
import java.util.*

private typealias ModelSong = com.github.bjoernpetersen.deskbot.api.swag.model.Song
private typealias ModelQueueEntry = com.github.bjoernpetersen.deskbot.api.swag.model.QueueEntry
private typealias ModelSongEntry = com.github.bjoernpetersen.deskbot.api.swag.model.SongEntry
private typealias ModelPlayerState = com.github.bjoernpetersen.deskbot.api.swag.model.PlayerState
private typealias ModelPlayerStateEnum = com.github.bjoernpetersen.deskbot.api.swag.model.PlayerState.StateEnum

fun convert(song: Song): ModelSong {
    val result = ModelSong()
    result.id = song.id
    result.providerId = song.providerName
    result.title = song.title
    result.description = song.description
    result.albumArtUrl = song.albumArtUrl.orElse(null)
    return result
}

fun convert(entry: QueueEntry): ModelQueueEntry {
    val queueEntry = ModelQueueEntry()
    queueEntry.userName = entry.user.name
    queueEntry.song = convert(entry.song)
    return queueEntry
}

fun convert(entry: SongEntry): ModelSongEntry {
    val song = convert(entry.song)
    val user = entry.user
    val userName = user?.name
    val queueEntry = ModelSongEntry()
    queueEntry.userName = userName
    queueEntry.song = song
    return queueEntry
}

fun convert(queue: List<QueueEntry>): List<ModelQueueEntry> = queue.map { convert(it) }

fun convert(playerState: PlayerState): ModelPlayerState {
    val result = ModelPlayerState()
    result.state = ModelPlayerStateEnum.valueOf(playerState.state.name)
    result.songEntry = playerState.entry
            .map { convert(it) }
            .orElse(null)
    return result
}

fun lookupProvider(manager: ProviderManager, providerId: String): Optional<Provider> =
        try {
            Optional.of(manager.getProvider(providerId))
        } catch (e: IllegalArgumentException) {
            Optional.empty()
        }

fun lookupSuggester(manager: ProviderManager, suggesterId: String): Optional<Suggester> =
        try {
            Optional.of(manager.getSuggester(suggesterId))
        } catch (e: IllegalArgumentException) {
            Optional.empty()
        }

fun lookupSong(manager: ProviderManager, songId: String, providerId: String): Optional<Song> =
        lookupProvider(manager, providerId)
                .map { provider ->
                    try {
                        provider.lookup(songId)
                    } catch (e: NoSuchSongException) {
                        null
                    }
                }