package net.bjoernpetersen.deskbot.view

import javafx.application.Platform
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.layout.Region
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.config.Config
import org.controlsfx.control.CheckListView

class DefaultPermissionConfig : Controller {
    private val logger = KotlinLogging.logger {}

    @FXML
    override lateinit var root: Region
        private set

    private val configEntryProperty: ObjectProperty<Config.SerializedEntry<Set<Permission>>> =
        SimpleObjectProperty()
    var configEntry by property(configEntryProperty)
    private val hasNoEntry = configEntryProperty.isNull

    @FXML
    private lateinit var permissionList: CheckListView<Permission>

    @FXML
    override fun initialize() {
        Platform.runLater { stage.title = DeskBot.resources["window.defaultPermission"] }
        permissionList.disableProperty().bind(hasNoEntry)
        permissionList.items.addAll(Permission.values())

        val permissions: MutableSet<Permission> = HashSet(Permission.values().size * 2)
        Permission.values().forEach { permission ->
            val checked = permissionList.getItemBooleanProperty(permission)
            checked.addListener { _, _, isEnabled ->
                logger.debug { "${permission.label} enabled: $isEnabled" }
                if (isEnabled) permissions.add(permission)
                else permissions.remove(permission)
                configEntry?.set(permissions)
            }
        }

        configEntryProperty.addListener { _, _, entry ->
            permissions.clear()
            entry?.get()?.also { permissions.addAll(it) }
            Permission.values().forEach {
                permissionList.getItemBooleanProperty(it).set(it in permissions)
            }
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private fun reset(actionEvent: ActionEvent) {
        configEntry?.let { entry ->
            entry.set(null)
            configEntry = null
            configEntry = entry
        }
    }

    @SuppressWarnings("unused")
    @FXML
    private fun close(actionEvent: ActionEvent) {
        closeWindow()
    }
}
