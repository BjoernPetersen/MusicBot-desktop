package net.bjoernpetersen.deskbot.view

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.layout.Region
import javafx.stage.Stage
import javafx.util.Callback
import net.bjoernpetersen.musicbot.api.config.Config
import net.bjoernpetersen.musicbot.api.plugin.PluginId

class PluginOrderConfig : Controller {
    @FXML
    override lateinit var root: Region
        private set

    private val loaded: ObjectProperty<List<PluginId>> = SimpleObjectProperty()
    var loadedPlugins by property(loaded)
    private val configEntryProperty: ObjectProperty<Config.SerializedEntry<List<PluginId>>> =
        SimpleObjectProperty()
    var configEntry by property(configEntryProperty)
    private val hasNoEntry = configEntryProperty.isNull

    @FXML
    private lateinit var pluginList: ListView<PluginOrderItem>

    @FXML
    override fun initialize() {
        pluginList.disableProperty().bind(hasNoEntry)
        pluginList.cellFactory = Callback {
            load<PluginOrderListCell>().apply {
                dragHandler = { fromIndex, toIndex ->
                    val items = pluginList.items
                    val item = items.removeAt(fromIndex)
                    items.add(toIndex, item)
                    true
                }
                contextMenu = ContextMenu(
                    MenuItem(DeskBot.resources["action.remove"]).apply {
                        setOnAction { event ->
                            item?.let { pluginList.items.remove(it) }
                            event.consume()
                        }
                    }
                ).apply {
                    setOnShown {
                        if (item == null || item.isAvailable) Platform.runLater { hide() }
                    }
                }
            }
        }

        configEntryProperty.addListener { _, _, _ -> refreshList() }
        loaded.addListener { _, _, _ -> refreshList() }
    }

    override fun getWindowTitle(): String? = DeskBot.resources["window.pluginOrder"]

    override fun onStageAttach(stage: Stage) {
        stage.setOnCloseRequest { event ->
            if (pluginList.items.map { it.pluginId } != configEntry?.get()) {
                val result = Alert(Alert.AlertType.CONFIRMATION).apply {
                    title = DeskBot.resources["confirmation.quitWithoutSaveTitle"]
                    headerText = DeskBot.resources["confirmation.quitWithoutSaveHeader"]
                }.showAndWait().orElse(ButtonType.CANCEL)
                if (result !== ButtonType.OK) {
                    event.consume()
                }
            }
        }
    }

    private fun refreshList() {
        val savedList = configEntry?.get()
            ?.mapTo(ArrayList()) { PluginOrderItem.unavailable(it) }
            ?: ArrayList()
        val loadedPlugins = loadedPlugins
        if (loadedPlugins != null) {
            for (plugin in loadedPlugins) {
                val wrapped = PluginOrderItem.available(plugin)
                val index = savedList.indexOf(wrapped)
                if (index < 0) savedList.add(0, wrapped)
                else savedList[index] = wrapped
            }
        }
        pluginList.items.setAll(savedList)
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun clearUnavailable(actionEvent: ActionEvent) {
        pluginList.items.removeIf { !it.isAvailable }
    }

    @Suppress("UNUSED_PARAMETER", "unused")
    @FXML
    private fun save(actionEvent: ActionEvent) {
        configEntry?.set(pluginList.items.map { it.pluginId })
        closeWindow()
    }
}
