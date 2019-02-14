package net.bjoernpetersen.deskbot.impl

import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.config.ChoiceBox
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.config.ConfigManager
import net.bjoernpetersen.musicbot.api.config.ConfigSerializer
import net.bjoernpetersen.musicbot.api.config.MainConfigScope
import net.bjoernpetersen.musicbot.api.config.NonnullConfigChecker
import net.bjoernpetersen.musicbot.api.config.SerializationException
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import net.bjoernpetersen.musicbot.spi.plugin.id
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager

class MainConfigEntries(
    configManager: ConfigManager,
    dependencyManager: DependencyManager,
    classLoader: ClassLoader) {

    private val plain = configManager[MainConfigScope].plain
    private val secret = configManager[MainConfigScope].secrets

    val defaultSuggester = configManager[MainConfigScope].plain.SerializedEntry(
        key = "defaultSuggester",
        description = "The suggester providing songs if the queue is empty",
        serializer = SuggesterSerializer(classLoader, dependencyManager),
        configChecker = { null },
        uiNode = ChoiceBox({ it.name }, { dependencyManager.findEnabledSuggester() }, true))

    val defaultPermissions: Config.SerializedEntry<Set<Permission>> = plain.SerializedEntry(
        key = "defaultPermissions",
        description = "",
        serializer = PermissionSetSerializer,
        configChecker = NonnullConfigChecker,
        default = Permission.getDefaults())

    val allPlain: List<Config.Entry<*>> = listOf(
        defaultSuggester
    )
    val allSecret: List<Config.Entry<*>> = listOf(
    )
}

private class SuggesterSerializer(
    private val classLoader: ClassLoader,
    private val dependencyManager: DependencyManager) : ConfigSerializer<Suggester> {

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
        return dependencyManager.getDefault(type.kotlin) ?: throw SerializationException()
    }
}

private object PermissionSetSerializer : ConfigSerializer<Set<Permission>> {
    private val logger = KotlinLogging.logger { }
    override fun deserialize(string: String): Set<Permission> {
        return string
            .split(',')
            .mapNotNull {
                try {
                    Permission.matchByLabel(it)
                } catch (e: IllegalArgumentException) {
                    logger.warn { "Unknown default permission: $it" }
                    null
                }
            }
            .toSet()
    }

    override fun serialize(obj: Set<Permission>): String {
        return obj.joinToString(",") { it.label }
    }

}
