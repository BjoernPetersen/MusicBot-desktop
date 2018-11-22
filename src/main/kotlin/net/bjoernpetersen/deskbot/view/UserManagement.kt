package net.bjoernpetersen.deskbot.view

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.layout.Region
import net.bjoernpetersen.musicbot.api.auth.User
import net.bjoernpetersen.musicbot.api.auth.UserManager

class UserManagement : Controller {
    @FXML
    override lateinit var root: Region
        private set

    private val res = DeskBot.resources
    private lateinit var userManager: UserManager

    @FXML
    private lateinit var userList: ListView<User>

    @FXML
    private lateinit var permissionManagementController: PermissionManagement

    @FXML
    override fun initialize() {
        Platform.runLater { stage.title = res["window.userManagement"] }
        userList.setCellFactory {
            TextFieldListCell(stringConverter { it?.name })
        }
        userList.selectionModel.selectedItemProperty().addListener { _, _, user ->
            permissionManagementController.setUser(userManager, user) { updatedUser ->
                val index = userList.items.indexOfFirst { updatedUser.name == it.name }
                userList.items[index] = updatedUser
            }
        }
    }

    fun setUserManager(userManager: UserManager) {
        this.userManager = userManager
        userList.items.setAll(userManager.getUsers())
    }
}
