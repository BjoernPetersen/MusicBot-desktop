package net.bjoernpetersen.deskbot.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.RadioButton
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import net.bjoernpetersen.musicbot.api.plugin.management.findDependencies
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager
import kotlin.reflect.KClass

class IdActivation : Controller {

    @FXML
    override lateinit var root: Pane
        private set

    private lateinit var dependencyManager: DependencyManager

    private val idProperty: ObjectProperty<KClass<out Plugin>> = SimpleObjectProperty()
    private var id by property(idProperty)

    @FXML
    private lateinit var idLabel: Label

    @FXML
    private lateinit var dependencyButton: Button
    @FXML
    private lateinit var disableButton: RadioButton

    @FXML
    private lateinit var implementationList: ListView<Plugin>

    private val enabledProperty: ObjectProperty<Plugin> = SimpleObjectProperty()
    private var enabled by property(enabledProperty)

    @FXML
    override fun initialize() {
        enabledProperty.bind(implementationList.selectionModel.selectedItemProperty())

        idLabel.textProperty()
            .bind(createStringBinding(idProperty) { id?.qualifiedName })

        dependencyButton.disableProperty().bind(disableButton.selectedProperty())
        disableButton.selectedProperty().addListener { _, _, isDisabled ->
            if (isDisabled) {
                id?.let {
                    dependencyManager.setDefault(null, it)
                }
                implementationList.selectionModel.select(null)
            }
        }

        implementationList.apply {
            setCellFactory {
                ListCell<Plugin>().also { listCell ->
                    val description = load<PluginDescription>()
                    listCell.graphic = description.root
                    description.pluginProperty.bind(listCell.itemProperty())
                }
            }
            selectionModel.selectedItemProperty().addListener { _, _, plugin ->
                if (plugin != null) {
                    this@IdActivation.id?.let { dependencyManager.setDefault(plugin, it) }
                    disableButton.isSelected = false
                }
            }
        }

        idProperty.addListener { _, _, id ->
            implementationList.items.clear()
            if (id != null) {
                val default = dependencyManager.getDefault(id)
                disableButton.isSelected = default == null
                dependencyManager.findAvailable(id).forEach {
                    implementationList.items.add(it)
                }
                implementationList.selectionModel.select(default)
            }
        }

        enabledProperty.addListener { _, _, enabled ->
            dependencyButton.textFill = if (enabled != null) {
                val satisfied = enabled.satisfiedDependencies()
                if (satisfied) Color.DARKGREEN else Color.RED
            } else Color.BLACK
        }
    }

    private fun Plugin.satisfiedDependencies(
        visited: MutableSet<Plugin> = HashSet(64)
    ): Boolean {
        val dependencies = findDependencies()
            .map { dependencyManager.getDefault(it) }
        visited.add(this)
        for (dependency in dependencies) {
            if (dependency == null) return false
            if (dependency !in visited && !dependency.satisfiedDependencies(visited)) {
                return false
            }
        }
        return true
    }

    fun setPlugin(dependencyManager: DependencyManager, id: KClass<out Plugin>?) {
        this.dependencyManager = dependencyManager
        this.id = id
    }

    @Suppress("UNUSED_PARAMETER")
    @FXML
    private fun showDependencies(actionEvent: ActionEvent) {
        enabled?.let {
            load<Dependencies>().apply {
                setPlugin(dependencyManager, it)
                show(modal = true)
            }
        }
    }
}
