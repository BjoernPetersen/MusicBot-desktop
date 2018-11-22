package net.bjoernpetersen.deskbot.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.layout.Region
import net.bjoernpetersen.musicbot.api.auth.Permission
import net.bjoernpetersen.musicbot.api.auth.User
import net.bjoernpetersen.musicbot.api.auth.UserManager
import org.controlsfx.control.CheckListView

class PermissionManagement : Controller {
    @FXML
    override lateinit var root: Region

    @FXML
    private lateinit var permissionList: CheckListView<Permission>

    private lateinit var userManager: UserManager
    private lateinit var update: (User) -> Unit
    private val userProperty: ObjectProperty<User> = SimpleObjectProperty()
    private var user by property(userProperty)

    @FXML
    override fun initialize() {
        permissionList.items.addAll(Permission.values())
        Permission.values().forEach { permission ->
            permissionList.getItemBooleanProperty(permission).addListener { _, _, value ->
                user?.let { user ->
                    val newPermissions = if (value) user.permissions + permission
                    else user.permissions - permission
                    val newUser = userManager.updateUser(user, newPermissions)
                    this.user = newUser
                    update(newUser)
                }
            }
        }
        userProperty.addListener { _, _, user ->
            if (user == null) {
                permissionList.isDisable = true
                permissionList.checkModel.clearChecks()
            } else {
                permissionList.isDisable = false
                val permissions = user.permissions
                Permission.values().forEach {
                    if (it in permissions) permissionList.checkModel.check(it)
                    else permissionList.checkModel.clearCheck(it)
                }
            }
        }
    }

    fun setUser(userManager: UserManager, user: User?, update: (User) -> Unit) {
        this.userManager = userManager
        this.update = update
        this.user = user
    }
}
