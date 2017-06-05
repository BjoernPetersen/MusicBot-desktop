package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.jmusicbot.user.Permission;
import com.github.bjoernpetersen.jmusicbot.user.User;
import com.github.bjoernpetersen.jmusicbot.user.UserManager;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javax.annotation.Nonnull;

class UserController {

  @Nonnull
  private final UserManager userManager;

  @FXML
  private ListView<User> userList;

  @FXML
  private Label userName;
  @FXML
  private VBox permissionBox;

  private ObjectProperty<User> activeUser;

  UserController(@Nonnull UserManager userManager) {
    this.userManager = userManager;
  }

  @FXML
  private void initialize() {
    activeUser = new SimpleObjectProperty<>();
    userList.getSelectionModel().selectedItemProperty()
        .addListener((observable, oldValue, newValue) -> activeUser.set(newValue));
    userList.setCellFactory(TextFieldListCell.forListView(new StringConverter<User>() {
      @Override
      public String toString(User object) {
        return object.getName();
      }

      @Override
      public User fromString(String string) {
        return null;
      }
    }));
    try {
      userList.setItems(FXCollections.observableList(userManager.getUsers()));
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }

    activeUser.addListener((observable, oldValue, newValue) -> {
      userName.setText(newValue == null ? "" : newValue.getName());
      permissionBox.getChildren().clear();
      if (newValue != null) {
        showPermissions(newValue);
      }
    });
  }

  Parent createNode() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(UserController.class.getResource("User.fxml"));
    loader.setController(this);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }


  private void showPermissions(@Nonnull User user) {
    Property<User> userProperty = new SimpleObjectProperty<>(user);
    for (Permission permission : Permission.values()) {
      permissionBox.getChildren().add(createPermissionNode(userProperty, permission));
    }
  }

  @Nonnull
  private Node createPermissionNode(@Nonnull Property<User> user, @Nonnull Permission permission) {
    return new PermissionController(user, permission, userManager).createNode();
  }
}
