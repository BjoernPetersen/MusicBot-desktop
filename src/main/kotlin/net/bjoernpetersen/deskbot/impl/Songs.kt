package net.bjoernpetersen.deskbot.impl

import net.bjoernpetersen.musicbot.ServerConstraints
import net.bjoernpetersen.musicbot.api.player.Song

internal val Song.localAlbumArtUrl: String?
    get() {
        return albumArtPath?.let { "http://localhost:${ServerConstraints.port}$it" }
    }
