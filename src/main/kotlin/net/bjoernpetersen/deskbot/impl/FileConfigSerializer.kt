package net.bjoernpetersen.deskbot.impl

import net.bjoernpetersen.musicbot.api.config.ConfigSerializer
import java.io.File

object FileConfigSerializer : ConfigSerializer<File> {
    override fun deserialize(string: String): File {
        return File(string)
    }

    override fun serialize(obj: File): String = obj.path
}
