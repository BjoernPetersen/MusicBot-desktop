package net.bjoernpetersen.deskbot.lifecycle

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import io.vertx.core.Vertx
import javafx.application.Platform
import javafx.concurrent.Task
import mu.KotlinLogging
import net.bjoernpetersen.deskbot.fximpl.FxInitStateWriter
import net.bjoernpetersen.deskbot.impl.FileConfigStorage
import net.bjoernpetersen.deskbot.impl.SongPlayedNotifierModule
import net.bjoernpetersen.deskbot.rest.RestModule
import net.bjoernpetersen.deskbot.rest.RestServer
import net.bjoernpetersen.deskbot.view.DeskBot
import net.bjoernpetersen.deskbot.view.get
import net.bjoernpetersen.deskbot.view.show
import net.bjoernpetersen.musicbot.api.config.ChoiceBox
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.config.ConfigManager
import net.bjoernpetersen.musicbot.api.config.ConfigSerializer
import net.bjoernpetersen.musicbot.api.config.MainConfigScope
import net.bjoernpetersen.musicbot.api.config.PluginConfigScope
import net.bjoernpetersen.musicbot.api.config.SerializationException
import net.bjoernpetersen.musicbot.api.module.BrowserOpenerModule
import net.bjoernpetersen.musicbot.api.module.ConfigModule
import net.bjoernpetersen.musicbot.api.module.DefaultPlayerModule
import net.bjoernpetersen.musicbot.api.module.DefaultQueueModule
import net.bjoernpetersen.musicbot.api.module.DefaultResourceCacheModule
import net.bjoernpetersen.musicbot.api.module.DefaultSongLoaderModule
import net.bjoernpetersen.musicbot.api.module.DefaultUserDatabaseModule
import net.bjoernpetersen.musicbot.api.module.InstanceStopper
import net.bjoernpetersen.musicbot.api.module.PluginClassLoaderModule
import net.bjoernpetersen.musicbot.api.module.PluginModule
import net.bjoernpetersen.musicbot.api.plugin.PluginLoader
import net.bjoernpetersen.musicbot.api.plugin.management.DefaultDependencyManager
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.api.plugin.management.findDependencies
import net.bjoernpetersen.musicbot.spi.player.Player
import net.bjoernpetersen.musicbot.spi.plugin.GenericPlugin
import net.bjoernpetersen.musicbot.spi.plugin.PlaybackFactory
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import net.bjoernpetersen.musicbot.spi.plugin.category
import net.bjoernpetersen.musicbot.spi.plugin.id
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager
import net.bjoernpetersen.musicbot.spi.util.BrowserOpener
import org.controlsfx.control.TaskProgressView
import java.io.File
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.thread
import kotlin.concurrent.withLock
import kotlin.reflect.KClass

class Lifecyclist {
    private val logger = KotlinLogging.logger {}

    var stage: Stage = Stage.New
        private set

    // Created stage vars
    private lateinit var configManager: ConfigManager
    private lateinit var classLoader: ClassLoader
    private lateinit var dependencyManager: DependencyManager
    private lateinit var defaultSuggester: Config.SerializedEntry<Suggester>

    // Injected stage vars
    private lateinit var pluginFinder: PluginFinder
    private lateinit var injector: Injector

    private fun <T> staged(stage: Stage, exact: Boolean = true, action: () -> T): T {
        if (exact) {
            if (this.stage != stage) throw IllegalStateException()
        } else {
            if (this.stage < stage) throw IllegalStateException()
        }
        return action()
    }

    fun getConfigManager() = staged(Stage.Created, false) { configManager }
    fun getPluginClassLoader() = staged(
        Stage.Created, false) { classLoader }

    fun getDependencyManager() = staged(
        Stage.Created, false) { dependencyManager }

    fun getDefaultSuggester() = staged(Stage.Created, false) { defaultSuggester }

    fun getPluginFinder() = staged(Stage.Injected, false) { pluginFinder }
    fun getInjector() = staged(Stage.Injected, false) { injector }

    private fun createConfig() {
        configManager = ConfigManager(
            FileConfigStorage(configDir),
            FileConfigStorage(secretDir),
            FileConfigStorage(stateDir)
        )
    }

    private fun createPlugins(pluginDir: File) {
        val loader = PluginLoader(pluginDir)
        dependencyManager = DefaultDependencyManager(configManager[MainConfigScope].state, loader)
        classLoader = loader.loader
    }

    fun create(pluginDir: File): DependencyManager = staged(Stage.New) {
        createConfig()
        createPlugins(pluginDir)

        defaultSuggester = configManager[MainConfigScope].state.SerializedEntry(
            key = "defaultSuggester",
            description = "The suggester providing songs if the queue is empty",
            serializer = SuggesterSerializer(classLoader, dependencyManager),
            configChecker = { null },
            uiNode = ChoiceBox({ it.name }, { dependencyManager.findEnabledSuggester() }, true))

        stage = Stage.Created
        dependencyManager
    }

    private fun modules(browserOpener: BrowserOpener, suggester: Suggester?): List<Module> = listOf(
        ConfigModule(configManager),
        DefaultPlayerModule(suggester),
        DefaultQueueModule(),
        DefaultSongLoaderModule(),
        DefaultUserDatabaseModule("jdbc:sqlite:UserDatabase.db"),
        PluginClassLoaderModule(classLoader),
        PluginModule(pluginFinder),
        BrowserOpenerModule(browserOpener),
        SongPlayedNotifierModule(),
        RestModule(),
        DefaultResourceCacheModule()
    )

    fun inject(browserOpener: BrowserOpener) = staged(Stage.Created) {
        pluginFinder = dependencyManager.finish()

        val suggester = defaultSuggester.get()
        logger.info { "Default suggester: ${suggester?.name}" }

        injector = Guice.createInjector(modules(browserOpener, suggester))
        sequenceOf(
            pluginFinder.genericPlugins,
            pluginFinder.playbackFactories,
            pluginFinder.providers,
            pluginFinder.suggesters)
            .flatMap { it.asSequence() }
            .forEach {
                injector.injectMembers(it)
            }

        pluginFinder.allPlugins().forEach {
            val configs = configManager[PluginConfigScope(it::class)]
            it.createConfigEntries(configs.plain)
            it.createSecretEntries(configs.secrets)
            it.createStateEntries(configs.state)
        }

        stage = Stage.Injected
    }

    fun run(result: (Throwable?) -> Unit) = staged(Stage.Injected) {
        // TODO rollback in case of failure
        Initializer(pluginFinder).start {
            if (it != null) {
                logger.error(it) { "Could not initialize!" }
                result(it)
                return@start
            }
            injector.getInstance(Player::class.java).start()

            val vertx = injector.getInstance(Vertx::class.java)
            val rest = injector.getInstance(RestServer::class.java)
            vertx.deployVerticle(rest)

            DeskBot.runningInstance = this
            stage = Stage.Running
            result(null)
        }
    }

    fun stop() = staged(Stage.Running) {
        val stopper = InstanceStopper(injector).apply {
            register(Vertx::class.java) { vertx ->
                val lock: Lock = ReentrantLock()
                val cond = lock.newCondition()
                vertx.close {
                    if (it.succeeded()) logger.debug { "Vertx close successful" }
                    else logger.error(it.cause()) { "Could not close Vertx" }

                    lock.withLock {
                        cond.signalAll()
                    }
                }

                lock.withLock { cond.await() }
            }
        }
        stopper.stop()
        stage = Stage.Stopped
    }

    enum class Stage {
        Stopped, New, Created, Injected, Running
    }

    private companion object {
        val stateDir = File("state");
        val configDir = File("config")
        val secretDir = File(configDir, "secret")
    }
}

private class Initializer(private val finder: PluginFinder) {

    private val logger = KotlinLogging.logger {}

    fun start(result: (Throwable?) -> Unit) {
        val view = TaskProgressView<Task<*>>()
        val tasks = view.tasks

        val lock: Lock = ReentrantLock()
        val done = lock.newCondition()!!
        val finished: MutableSet<Plugin> = HashSet(64)
        val errors: MutableList<Throwable> = ArrayList()

        val res = DeskBot.resources
        val window = view.show(modal = true, title = res["window.initialization"])

        val parentTask = object : Task<Unit>() {
            override fun call() {
                updateTitle(res["task.parent.title"])
                updateMessage(res["task.parent.description"])

                // TODO timeout/cancel
                val todo = finder.run {
                    genericPlugins.size + playbackFactories.size + providers.size + suggesters.size
                }.toLong()
                updateProgress(0, todo)
                lock.withLock {
                    var finishedCount = finished.size.toLong()
                    while (finishedCount != todo) {
                        updateProgress(finishedCount, todo)
                        done.await()
                        finishedCount = finished.size.toLong()
                    }
                }

                val exception = if (errors.isEmpty()) null
                else errors.fold(Exception("One or more initializations failed")) { e, t ->
                    e.apply { addSuppressed(t) }
                }

                Platform.runLater {
                    window.close()
                    result(exception)
                }
            }
        }
        thread(name = "InitializationParent", isDaemon = true) { parentTask.run() }
        tasks.add(parentTask)

        finder.allPlugins().forEach { plugin ->
            val task = object : Task<Unit>() {
                val writer = FxInitStateWriter(::updateTitle, ::updateMessage)

                override fun call() {
                    writer.begin(plugin)
                    plugin.findDependencies()
                        .asSequence()
                        .map { finder[it]!! }
                        .forEach {
                            if (it !in finished) {
                                val type = it.category.simpleName
                                lock.withLock {
                                    while (it !in finished) {
                                        writer.state(
                                            res["task.plugin.waiting"].format(type, it.name))
                                        done.await()
                                    }
                                }
                            }
                        }
                    writer.state("Starting...")

                    try {
                        plugin.initialize(writer)
                    } catch (e: Throwable) {
                        logger.error(e) { "Could not initialize $plugin" }
                        throw e
                    } finally {
                        writer.close()
                    }

                    lock.withLock {
                        finished.add(plugin)
                        done.signalAll()
                    }
                }
            }
            tasks.add(task)
            thread(isDaemon = true, name = "Init${plugin.category.simpleName}${plugin.name}") {
                task.run()
            }
        }
    }
}

private fun Plugin.findSpecialized(): KClass<out Plugin> {
    return when (this) {
        is GenericPlugin -> GenericPlugin::class
        is PlaybackFactory -> PlaybackFactory::class
        is Provider -> Provider::class
        is Suggester -> Suggester::class
        else -> throw IllegalArgumentException()
    }
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
