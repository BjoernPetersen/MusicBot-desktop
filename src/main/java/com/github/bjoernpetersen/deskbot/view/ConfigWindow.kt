package com.github.bjoernpetersen.deskbot.view

import com.github.bjoernpetersen.deskbot.DeskBot
import com.github.bjoernpetersen.deskbot.api.Broadcaster
import com.github.bjoernpetersen.deskbot.api.RestApi
import com.github.bjoernpetersen.deskbot.model.*
import com.github.bjoernpetersen.deskbot.view.config.WeakConfigListener
import com.github.bjoernpetersen.deskbot.view.config.createNode
import com.github.bjoernpetersen.jmusicbot.*
import com.github.bjoernpetersen.jmusicbot.config.Config
import com.github.bjoernpetersen.jmusicbot.config.ConfigChecker
import com.github.bjoernpetersen.jmusicbot.config.ConfigListener
import com.github.bjoernpetersen.jmusicbot.config.ui.Choice
import com.github.bjoernpetersen.jmusicbot.config.ui.ChoiceBox
import com.github.bjoernpetersen.jmusicbot.config.ui.DefaultStringConverter
import com.github.bjoernpetersen.jmusicbot.platform.Platform
import com.github.bjoernpetersen.jmusicbot.platform.Support
import com.github.bjoernpetersen.jmusicbot.provider.ProviderManager
import com.github.bjoernpetersen.jmusicbot.provider.Suggester
import com.github.bjoernpetersen.jmusicbot.user.UserManager
import com.github.zafarkhaja.semver.Version
import io.sentry.Sentry
import io.sentry.event.User
import javafx.beans.property.ListProperty
import javafx.beans.property.SimpleListProperty
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.control.Alert
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.sql.SQLException
import java.util.*
import java.util.UUID
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class PluginTypePane : Fragment() {
  val plugins: ObservableList<ObservablePluginWrapper<*>> by param()
  private val selectedPlugin = PluginModel()
  override val root = borderpane {
    left {
      listview(plugins) {
        selectedPlugin.rebindOnChange(this) { item = it }
        cellFormat {
          graphic = cache {
            form {
              fieldset {
                field("Name") {
                  label(it.readableName)
                }
                field("Enabled") {
                  label().textProperty().bind(it.enabledProperty().stringBinding {
                    it.toString()
                  })
                }
              }
            }
          }
        }
      }
    }
    center {
      this += find<PluginDetails>(mapOf(
          PluginDetails::pluginModel to selectedPlugin))
    }
  }
}

class PluginModel : ItemViewModel<ObservablePluginWrapper<*>>() {
  val plugin = bind(ObservablePluginWrapper<*>::wrapped)
  val isEnabled = bind(ObservablePluginWrapper<*>::enabledProperty)
  val name = bind(ObservablePluginWrapper<*>::getReadableName)
  val entries = bind(ObservablePluginWrapper<*>::observableConfigEntries)

  init {
    isEnabled.onChange { commit() }
  }
}

class PluginDetails : Fragment() {
  val pluginModel: PluginModel by param()
  override val root = vbox {
    visibleProperty().bind(pluginModel.empty.not())
    label { bind(pluginModel.name) }
    val enabledBox = checkbox("Enabled") {
      selectedProperty().bindBidirectional(pluginModel.isEnabled)
    }

    val configPane = find<ConfigPane>(mapOf(
        ConfigPane::entries to pluginModel.entries))
    configPane.root.visibleProperty().bind(enabledBox.selectedProperty())
    this += configPane
  }
}

class MissingConfigDialog : Fragment() {
  val plugin: String by param()
  var result: Configurator.Result = Configurator.Result.CANCEL
    private set
  override val root = borderpane {
    top = label("Missing config entries for ${plugin}")
    center = find<ConfigPane>(params).root
    bottom = hbox {
      button("Cancel") {
        setOnAction {
          result = Configurator.Result.CANCEL
          close()
        }
      }
      button("Disable") {
        setOnAction {
          result = Configurator.Result.DISABLE
          close()
        }
      }
      button("OK") {
        setOnAction {
          result = Configurator.Result.OK
          close()
        }
      }
    }
  }
}

class ConfigPane : Fragment() {
  val entries: ListProperty<Config.Entry> by param()
  val listeners: MutableList<ConfigListener<String?>> = LinkedList()
  override val root = form {
    fieldset {
      bindChildren(entries, { entry ->
        field(entry.key) {
          label(entry.description)
          this += createNode({ currentWindow!! }, entry)
          if (entry is Config.ReadOnlyStringEntry) this += createWarning(entry)
        }
      })
    }
  }

  private fun createWarning(entry: Config.ReadOnlyStringEntry): Node {
    val warningNode = Label()
    warningNode.styleClass.addAll("alert", "warning", "dialog-pane")
    val tooltip = Tooltip()
    Tooltip.install(warningNode, tooltip)
    val listener = { oldValue: String?, newValue: String? ->
      val warning = entry.checkError()
      if (warning != null) {
        tooltip.text = warning
      }
      warningNode.isVisible = warning != null
    }
    listeners.add(listener)
    entry.addListener(WeakConfigListener(entry, listener))
    listener(null, entry.value)
    return warningNode
  }

  override fun onUndock() {
    super.onUndock()
    listeners.clear()
  }
}

class ConfigWindow : Fragment("DeskBot Config"), Loggable {
  private val controller: ConfigController by inject()

  override val root = vbox {
    tabpane {
      vgrow = Priority.ALWAYS
      tab(messages["general"]) {
        isClosable = false
        val general = controller.botData.config.defaults.entries.toMutableList()
        general.add(controller.defaultSuggester)
        this += find<ConfigPane>(mapOf(
            ConfigPane::entries to SimpleListProperty(general.observable())))
      }
      tab("PlaybackFactory") {
        isClosable = false
        this += find<PluginTypePane>(mapOf(
            PluginTypePane::plugins to
                controller.botData.playbackFactoryManager.playbackFactories.toList().observable()))
      }
      tab("Provider") {
        isClosable = false
        this += find<PluginTypePane>(mapOf(
            PluginTypePane::plugins to
                controller.botData.providerManager.allProviders.values.toList().observable()))
      }
      tab("Suggester") {
        isClosable = false
        this += find<PluginTypePane>(mapOf(
            PluginTypePane::plugins to
                controller.botData.providerManager.allSuggesters.values.toList().observable()))
      }
      tab("AdminPlugin") {
        isClosable = false
        this += find<PluginTypePane>(mapOf(
            PluginTypePane::plugins to controller.botData.adminPlugins))
      }
    }
    hbox {
      vgrow = Priority.NEVER
      spacing = 5.0
      button("Start") {
        hgrow = Priority.ALWAYS
        maxWidth = Double.MAX_VALUE
        setOnAction { startLoading() }
      }
      button("Exit") {
        hgrow = Priority.ALWAYS
        maxWidth = Double.MAX_VALUE
        onAction = EventHandler<ActionEvent> {
          primaryStage.close()
        }
      }
    }
  }

  private fun startLoading() {
    val data = controller.botData
    val builder = MusicBot.Builder(data.config)
        .providerManager(data.providerManager)
        .playbackFactoryManager(data.playbackFactoryManager)
        .defaultSuggester(controller.lookupDefaultSuggester())
        .apiInitializer(::RestApi)
        .broadcasterInitializer(::Broadcaster)
        .configurator(this@ConfigWindow::askForMissingConfig)

    data.adminPlugins.forEach { builder.addAdminPlugin(it) }

    try {
      builder.userManager(UserManager(data.config, "jdbc:sqlite:users2.db"))
    } catch (e: SQLException) {
      logSevere(e, "Could not connect to database")
      return
    }

    replaceWith(find<LoadingWindow>(mapOf(LoadingWindow::builder to builder)))
  }

  override fun onDock() {
    super.onDock()
    if (firstRun && (app as DeskBot).noConfig) {
      firstRun = false
      startLoading()
      return
    }
    primaryStage.sizeToScene()
  }

  private fun askForMissingConfig(plugin: String, entries: List<Config.Entry>): Configurator.Result {
    val lock = ReentrantLock()
    val done = lock.newCondition()
    var result = Configurator.Result.OK
    val ask = {
      val modal = find<MissingConfigDialog>(mapOf(
          MissingConfigDialog::plugin to plugin,
          ConfigPane::entries to SimpleListProperty(entries.observable())
      )).apply { openModal(block = true) }
      result = modal.result
      lock.withLock {
        done.signalAll()
      }
    }

    lock.withLock {
      if (!runOnUi(ask))
        done.await()
    }

    return result
  }

  companion object {
    private var firstRun = true
  }
}

class BotData(val config: Config, private val ignoreOutdated: Boolean) : Loggable {
  val playbackFactoryManager = PlaybackFactoryManager(config, { ObservablePlaybackFactoryWrapper(config, it) })
  val providerManager = ProviderManager.defaultManager(
      { ObservableProviderWrapper(config, it) },
      { ObservableSuggesterWrapper(config, it) }
  )
  var adminPlugins = loadAdminPlugins()

  init {
    configureSentryUser(config)
    providerManager.initialize(config, playbackFactoryManager)
  }

  private fun configureSentryUser(config: Config) {
    val userIdEntry = config.StringEntry(javaClass, "sentryUser", "", true)
    val storedId = userIdEntry.value
    val userId: String
    if (storedId == null) {
      userId = UUID.randomUUID().toString()
      userIdEntry.set(userId)
    } else {
      userId = storedId
    }
    logFine("Sentry user ID: " + userId)
    val user = User(userId, null, null, null)
    Sentry.setUser(user)
  }

  private fun loadAdminPlugins(): ObservableList<ObservableAdminPluginWrapper> {
    val pluginFolderName = config.defaults.pluginFolder.value!!
    val pluginFolder = File(pluginFolderName)
    val loader = PluginLoader(pluginFolder, AdminPlugin::class.java)

    val plugins: Collection<AdminPlugin> = try {
      loader.load()
    } catch (e: AbstractMethodError) {
      showOutdated(e)
      throw InitializationException("Outdated plugin")
    }

    val result = ArrayList<ObservableAdminPluginWrapper>(plugins.size)
    val version = MusicBot.getVersion()
    for (plugin in plugins) {
      val wrapper = ObservableAdminPluginWrapper(config, plugin)
      checkCompatible(version, wrapper)
      result.add(wrapper)
    }

    return result.observable()
  }

  private fun checkCompatible(version: Version, plugin: PluginWrapper<*>) {
    if (plugin.minSupportedVersion.greaterThan(version) || plugin.maxSupportedVersion.lessThan(version)) {
      if (!ignoreOutdated || !plugin.maxSupportedVersion.lessThan(version)) {
        showIncompatible(plugin)
        throw IllegalStateException()
      } else {
        logInfo("Allowing technically incompatible plugin " + plugin.readableName)
      }
    }
    if (plugin.getSupport(Platform.get()) == Support.NO) {
      plugin.destructConfigEntries()
      logInfo("Plugin " + plugin.readableName + " does not support the current platform.")
    }
  }

  private fun showIncompatible(p: Plugin) {
    logInfo("A plugin is incompatible: %s", p.readableName)
    val alert = Alert(Alert.AlertType.ERROR)
    alert.isResizable = true
    alert.headerText = "Incompatible plugin"
    val stringBuilder = ("Plugin name: " + p.readableName + '\n'
        + "Supported range: " + p.minSupportedVersion
        + " - " + p.maxSupportedVersion + '\n'
        + "(Current version is " + MusicBot.getVersion() + ')')
    alert.contentText = stringBuilder
    alert.showAndWait()
    System.exit(0)
  }

  private fun showOutdated(e: AbstractMethodError) {
    logInfo(e, "A plugin is outdated")
    val alert = Alert(Alert.AlertType.ERROR)
    alert.isResizable = true
    alert.headerText = "Outdated plugin"
    val writer = StringWriter()
    e.printStackTrace(PrintWriter(writer))
    alert.contentText = writer.toString()
    alert.showAndWait()
    System.exit(0)
  }
}

class ConfigController : Controller() {
  var botData = createData()
    private set

  private fun createData(): BotData = BotData(
      Config(ConfigStorage(File("config.properties"), File("secrets.properties")),
          JavaFxHostServices()),
      (app as DeskBot).ignoreOutdated)

  var defaultSuggester = createDefaultSuggesterEntry()
    private set

  private fun createDefaultSuggesterEntry(): Config.StringEntry = botData.config.StringEntry(
      ConfigController::class.java,
      "defaultSuggester",
      "Suggester to play songs from if queue is empty",
      false,
      null,
      createSuggesterChoice(),
      ConfigChecker { if (lookupSuggester(it) != null) null else "Not an active suggester" })

  private fun createSuggesterChoice(): ChoiceBox<String, SuggesterChoice> = ChoiceBox(
      {
        botData.providerManager.allSuggesters.values.asSequence()
            .filter { it.state == Plugin.State.CONFIG }
            .map { SuggesterChoice(it) }
            .toList()
      },
      DefaultStringConverter,
      lazy = false)

  fun lookupDefaultSuggester(): Suggester? {
    val id = defaultSuggester.value ?: return null
    return lookupSuggester(id)
  }

  private fun lookupSuggester(id: String): Suggester? {
    val suggester = botData.providerManager.allSuggesters[id]
    return if (suggester?.state == Plugin.State.CONFIG) suggester else null
  }

  fun reload() {
    botData = createData()
    defaultSuggester.destruct()
    defaultSuggester = createDefaultSuggesterEntry()
  }
}

private class SuggesterChoice(suggester: Suggester) : Choice<String> {
  override val id: String = suggester.id
  override val displayName = suggester.readableName
}
