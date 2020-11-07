package net.bjoernpetersen.deskbot.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.control.TreeView
import javafx.scene.control.cell.ChoiceBoxTreeCell
import javafx.scene.layout.Region
import net.bjoernpetersen.deskbot.view.tree.PluginTreeItem
import net.bjoernpetersen.musicbot.api.plugin.category
import net.bjoernpetersen.musicbot.api.plugin.id
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager

class Dependencies : Controller {
    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var tree: TreeView<Plugin>

    private lateinit var dependencyManager: DependencyManager
    private val pluginProperty: ObjectProperty<Plugin> = SimpleObjectProperty()
    private var plugin by property(pluginProperty)

    @FXML
    override fun initialize() {
        pluginProperty.addListener { _, _, plugin ->
            tree.root = if (plugin == null) null
            else PluginTreeItem(dependencyManager, plugin.id.type, plugin).apply {
                isExpanded = true
            }
        }
        tree.setCellFactory {
            // TODO i18n
            ChoiceBoxTreeCell<Plugin>(
                stringConverter { plugin ->
                    plugin?.let { "${it.name} (${it.category.simpleName})" } ?: "<Please select>"
                }
            ).apply {
                treeItemProperty().addListener { _, _, treeItem ->
                    if (treeItem is PluginTreeItem) {
                        items.setAll(dependencyManager.findAvailable(treeItem.base))
                    } else {
                        items.clear()
                    }
                }
                itemProperty().addListener { _, _, item ->
                    (treeItem as? PluginTreeItem)?.let { treeItem ->
                        dependencyManager.setDefault(item, treeItem.base)
                        treeItem.value = item
                    }
                }
            }
        }
    }

    override fun getWindowTitle(): String? {
        return DeskBot.resources.getString("window.dependencies")
    }

    fun setPlugin(dependencyManager: DependencyManager, plugin: Plugin) {
        this.dependencyManager = dependencyManager
        this.plugin = plugin
    }
}
