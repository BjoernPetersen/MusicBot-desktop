package net.bjoernpetersen.deskbot.impl

import net.bjoernpetersen.musicbot.api.config.ConfigManager
import net.bjoernpetersen.musicbot.api.config.ConfigSerializer
import net.bjoernpetersen.musicbot.api.config.GenericConfigScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class FileConfigTest {
    @Test
    fun storeWorks() {
        val adapter = FileConfigStorage(File("config"))
        val config = ConfigManager(adapter, adapter, adapter)
        val entry = config[GenericConfigScope(FileConfigTest::class)]
            .state
            .SerializedEntry(
                "testKey",
                "",
                ClassSerializer,
                { null }
            )
        entry.set(File::class.java)
        assertEquals(File::class.java, entry.get())
    }
}

object ClassSerializer : ConfigSerializer<Class<*>> {
    override fun deserialize(string: String): Class<*> {
        return Class.forName(string)
    }

    override fun serialize(obj: Class<*>): String {
        return obj.name
    }
}
