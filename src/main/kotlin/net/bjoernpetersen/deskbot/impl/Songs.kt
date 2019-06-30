package net.bjoernpetersen.deskbot.impl

import net.bjoernpetersen.musicbot.api.player.Song
import net.bjoernpetersen.musicbot.spi.image.ImageServerConstraints

internal val Song.localAlbumArtUrl: String?
    get() {
        return albumArtPath?.let { "http://localhost:${ImageServerConstraints.PORT}$it" }
    }

@Suppress("DEPRECATION")
internal val Song.effectiveAlbumArtUrl: String?
    get() {
        return localAlbumArtUrl ?: albumArtUrl
    }
