package net.bjoernpetersen.deskbot

import net.bjoernpetersen.musicbot.api.config.ConfigManager
import net.bjoernpetersen.musicbot.api.config.GenericConfigScope
import net.bjoernpetersen.musicbot.api.plugin.PluginLoader
import net.bjoernpetersen.musicbot.api.plugin.management.DefaultDependencyManager
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager
import java.io.File
import kotlin.reflect.KClass

object PluginHandler {
    private const val PLUGIN_DIR = "plugins"
    lateinit var classLoader: ClassLoader

    fun loadPlugins(configManager: ConfigManager): DependencyManager {
        val loader = PluginLoader(File(PLUGIN_DIR))
        classLoader = loader.loader
        val managerState = configManager[GenericConfigScope(DependencyManager::class)].state
        return DefaultDependencyManager(managerState, loader)
    }

    @Deprecated("CHECK BEFORE USING", level = DeprecationLevel.HIDDEN)
    fun findAllRequirements(manager: DependencyManager): Map<KClass<out Plugin>, List<Plugin>> {
        return manager.run { findEnabledDependencies() }
            .associateWith {
                manager.findAvailable(it)
            }
    }
}
