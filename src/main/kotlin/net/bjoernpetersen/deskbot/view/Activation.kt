package net.bjoernpetersen.deskbot.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.Tooltip
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.layout.Region
import net.bjoernpetersen.musicbot.api.plugin.DeclarationException
import net.bjoernpetersen.musicbot.api.plugin.id
import net.bjoernpetersen.musicbot.api.plugin.pluginId
import net.bjoernpetersen.musicbot.spi.plugin.GenericPlugin
import net.bjoernpetersen.musicbot.spi.plugin.PlaybackFactory
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.Provider
import net.bjoernpetersen.musicbot.spi.plugin.Suggester
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager
import kotlin.reflect.KClass

class Activation : Controller {
    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var idList: ListView<KClass<out Plugin>>

    @FXML
    private lateinit var idActivationController: IdActivation

    private lateinit var dependencyManager: DependencyManager
    private val typeProperty: ObjectProperty<KClass<out Plugin>> = SimpleObjectProperty()
    private var type by property(typeProperty)

    @FXML
    override fun initialize() {
        idList.setCellFactory {
            TextFieldListCell<KClass<out Plugin>>(
                stringConverter {
                    it?.pluginId?.displayName
                }
            ).apply {
                val tooltip = Tooltip()
                itemProperty().addListener { _, _, id ->
                    if (id != null) {
                        tooltip.text = id.qualifiedName
                        setTooltip(tooltip)
                    } else setTooltip(null)
                }
            }
        }

        idList.selectionModel.selectedItemProperty().addListener { _, _, id ->
            idActivationController.setPlugin(dependencyManager, id)
        }

        typeProperty.addListener { _, _, type ->
            val plugins = when (type) {
                GenericPlugin::class -> dependencyManager.genericPlugins
                PlaybackFactory::class -> dependencyManager.playbackFactories
                Provider::class -> dependencyManager.providers
                Suggester::class -> dependencyManager.suggesters
                else -> throw IllegalStateException("Unknown plugin type: $type")
            }
            populateIdList(plugins)
        }
    }

    private fun populateIdList(plugins: List<Plugin>) {
        idList.items.clear()
        plugins
            .mapNotNull {
                try {
                    it.id
                } catch (e: DeclarationException) {
                    null
                }
            }
            .distinct()
            .forEach { idList.items.add(it.type) }
    }

    fun setData(dependencyManager: DependencyManager, type: KClass<out Plugin>) {
        this.dependencyManager = dependencyManager
        this.type = type
    }

    inline fun <reified T : Plugin> setData(dependencyManager: DependencyManager) {
        setData(dependencyManager, T::class)
    }
}
