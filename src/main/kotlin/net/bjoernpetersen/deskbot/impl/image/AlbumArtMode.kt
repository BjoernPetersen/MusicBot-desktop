package net.bjoernpetersen.deskbot.impl.image

import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.config.ConfigSerializer
import net.bjoernpetersen.musicbot.api.config.SerializationException

enum class AlbumArtMode(
    val friendlyName: String,
    val showCurrent: Boolean,
    val showListItem: Boolean
) {
    NONE("None", false, false),
    CURRENT("Only current song", true, false),
    ALL("Unrestricted", true, true);

    companion object : ConfigSerializer<AlbumArtMode> {
        private val logger = KotlinLogging.logger {}

        override fun serialize(obj: AlbumArtMode): String {
            return obj.name
        }

        override fun deserialize(string: String): AlbumArtMode {
            return try {
                valueOf(string)
            } catch (e: IllegalArgumentException) {
                logger.warn { "Could not deserialize AlbumArtMode $string" }
                throw SerializationException()
            }
        }
    }
}
