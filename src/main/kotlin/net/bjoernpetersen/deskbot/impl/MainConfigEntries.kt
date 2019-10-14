package net.bjoernpetersen.deskbot.impl

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import net.bjoernpetersen.deskbot.view.DefaultPermissionConfig
import net.bjoernpetersen.deskbot.view.DeskBot
import net.bjoernpetersen.deskbot.view.get
import net.bjoernpetersen.deskbot.view.load
import net.bjoernpetersen.deskbot.view.show
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.config.ChoiceBox
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.config.ConfigManager
import net.bjoernpetersen.musicbot.api.config.ConfigSerializer
import net.bjoernpetersen.musicbot.api.config.MainConfigScope
import net.bjoernpetersen.musicbot.api.config.NonnullConfigChecker
import net.bjoernpetersen.musicbot.api.config.PathSerializer
import net.bjoernpetersen.musicbot.api.config.SerializationException
import net.bjoernpetersen.musicbot.api.config.actionButton
import net.bjoernpetersen.musicbot.api.config.openDirectory
import net.bjoernpetersen.musicbot.api.config.serialization
import net.bjoernpetersen.musicbot.api.config.serialized
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import net.bjoernpetersen.musicbot.spi.plugin.id
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.inject.Inject
import javax.inject.Named

class MainConfigEntries @Inject constructor(
    configManager: ConfigManager,
    pluginFinder: PluginFinder,
    @Named("PluginClassLoader")
    classLoader: ClassLoader
) {

    private val plain = configManager[MainConfigScope].plain
    private val secret = configManager[MainConfigScope].secrets

    val defaultSuggester by plain.serialized<Suggester> {
        description = "The suggester providing songs if the queue is empty"
        serializer = SuggesterSerializer(classLoader, pluginFinder)
        check { null }
        uiNode = ChoiceBox({ it.name }, { pluginFinder.suggesters })
    }

    val defaultPermissions by plain.serialized<Set<Permission>> {
        description = "Permissions for new users and guests"
        serializer = PermissionSetSerializer
        check(NonnullConfigChecker)
        actionButton {
            label = DeskBot.resources["action.edit"]
            describe { it.sorted().joinToString() }
            val mutex = Mutex()
            action {
                mutex.withLock {
                    withContext(Dispatchers.Main) {
                        load<DefaultPermissionConfig>().apply {
                            configEntry = defaultPermissions
                            root.show(modal = true, wait = true)
                        }
                    }
                }
                true
            }
        }
        default(Permission.getDefaults())
    }

    val storageDir by plain.serialized<Path> {
        description = "Directory to store plugin files in." +
            " This should preferably be somewhere with a lot of free space."
        serializer = PathSerializer
        check {
            if (it != null && Files.isDirectory(it)) null
            else "Must be an existing directory"
        }
        openDirectory()
        default(Paths.get("storage"))
    }

    val loadAlbumArt by plain.serialized<AlbumArtMode> {
        description = "Which album art images to load. Disable to save bandwidth."
        serializer = AlbumArtMode
        check(NonnullConfigChecker)
        uiNode = ChoiceBox({ it.friendlyName }, { AlbumArtMode.values().asList() })
        default(AlbumArtMode.ALL)
    }

    val allPlain: List<Config.Entry<*>> = listOf(
        defaultSuggester,
        storageDir,
        loadAlbumArt,
        defaultPermissions
    )
    val allSecret: List<Config.Entry<*>> = listOf()
}

private class SuggesterSerializer(
    private val classLoader: ClassLoader,
    private val pluginFinder: PluginFinder
) : ConfigSerializer<Suggester> {

    override fun serialize(obj: Suggester): String {
        return obj.id.qualifiedName!!
    }

    @Suppress("UNCHECKED_CAST")
    override fun deserialize(string: String): Suggester {
        val type = try {
            classLoader.loadClass(string)
        } catch (e: ClassNotFoundException) {
            throw SerializationException()
        } as Class<out Suggester>
        return pluginFinder[type.kotlin] ?: throw SerializationException()
    }
}

private val PermissionSetSerializer = serialization<Set<Permission>> {
    serialize { permissions ->
        if (permissions.isEmpty()) "NONE"
        else permissions.joinToString(",") { it.label }
    }
    deserialize {
        if (it == "NONE") emptySet()
        else it
            .split(',')
            .mapNotNull {
                try {
                    Permission.matchByLabel(it)
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            .toSet()
    }
}
