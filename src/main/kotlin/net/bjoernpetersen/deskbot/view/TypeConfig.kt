package net.bjoernpetersen.deskbot.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.layout.Region
import net.bjoernpetersen.deskbot.lifecycle.Lifecyclist
import net.bjoernpetersen.deskbot.view.property.ConfigEntryEditorFactory
import net.bjoernpetersen.deskbot.view.property.ConfigEntryItem
import net.bjoernpetersen.musicbot.api.config.ConfigManager
import net.bjoernpetersen.musicbot.api.config.PluginConfigScope
import net.bjoernpetersen.musicbot.api.plugin.management.PluginFinder
import net.bjoernpetersen.musicbot.spi.plugin.GenericPlugin
import net.bjoernpetersen.musicbot.spi.plugin.PlaybackFactory
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import org.controlsfx.control.PropertySheet
import kotlin.reflect.KClass

class TypeConfig : Controller {
    @FXML
    override lateinit var root: Region
        private set

    private lateinit var finder: PluginFinder
    private lateinit var configManager: ConfigManager
    private val typeProperty: ObjectProperty<KClass<out Plugin>> = SimpleObjectProperty()
    private var type by property(typeProperty)

    @FXML
    private lateinit var pluginList: ListView<Plugin>
    @FXML
    private lateinit var pluginDescriptionController: PluginDescription
    @FXML
    private lateinit var propertySheet: PropertySheet

    @FXML
    override fun initialize() {
        pluginList.setCellFactory { TextFieldListCell(stringConverter { it?.name }) }
        setUpPropertySheet()

        pluginList.selectionModel.selectedItemProperty().addListener { _, _, plugin ->
            pluginDescriptionController.plugin = plugin
        }

        typeProperty.addListener { _, _, type ->
            if (type == null) pluginList.items.clear()
            else {
                val plugins = when (type) {
                    GenericPlugin::class -> finder.genericPlugins
                    PlaybackFactory::class -> finder.playbackFactories
                    Provider::class -> finder.providers
                    Suggester::class -> finder.suggesters
                    else -> throw IllegalStateException("Unknown plugin type: $type")
                }
                pluginList.items.setAll(plugins)
            }
        }
    }

    private fun setUpPropertySheet() {
        propertySheet.propertyEditorFactory = ConfigEntryEditorFactory()
        pluginList.selectionModel.selectedItemProperty().addListener { _, _, item ->
            propertySheet.items.clear()
            if (item != null) {
                val configs = configManager[PluginConfigScope(item::class)]
                item.createStateEntries(configs.state)
                val plain = item.createConfigEntries(configs.plain)
                val secret = item.createSecretEntries(configs.secrets)
                plain
                    .map { ConfigEntryItem.forEntry(it, false) }
                    .forEach { propertySheet.items.add(it) }

                secret
                    .map { ConfigEntryItem.forEntry(it, true) }
                    .forEach { propertySheet.items.add(it) }
            }
        }
    }

    fun setType(lifecycle: Lifecyclist, type: KClass<out Plugin>) {
        this.finder = lifecycle.getPluginFinder()
        this.configManager = lifecycle.getConfigManager()
        this.type = type
    }
}
