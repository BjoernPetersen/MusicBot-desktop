package com.github.bjoernpetersen.deskbot.view;

import com.github.bjoernpetersen.jmusicbot.user.Permission;
import com.github.bjoernpetersen.jmusicbot.user.User;
import com.github.bjoernpetersen.jmusicbot.user.UserManager;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javafx.beans.property.Property;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javax.annotation.Nonnull;

class PermissionController {

  @Nonnull
  private static final Logger log = Logger.getLogger(PermissionController.class.getName());

  @Nonnull
  private Property<User> user;
  @Nonnull
  private final Permission permission;
  @Nonnull
  private final UserManager userManager;

  @FXML
  private Label name;
  @FXML
  private CheckBox activeToggle;

  PermissionController(@Nonnull Property<User> user, @Nonnull Permission permission,
      @Nonnull UserManager userManager) {
    this.user = user;
    this.permission = permission;
    this.userManager = userManager;
  }

  Node createNode() {
    FXMLLoader loader = new FXMLLoader();
    loader.setLocation(PermissionController.class.getResource("Permission.fxml"));
    loader.setController(this);
    try {
      return loader.load();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Nonnull
  private User getUser() {
    return user.getValue();
  }

  private void setUser(@Nonnull User user) {
    this.user.setValue(user);
  }

  @FXML
  private void initialize() {
    name.setText(permission.getName());
    activeToggle.setSelected(getUser().getPermissions().contains(permission));

    activeToggle.selectedProperty().addListener((observable, oldValue, newValue) -> {
      User user = getUser();
      if (user.isInvalid()) {
        log.warning("Invalid user");
        return;
      }
      Set<Permission> permissions = new HashSet<>(user.getPermissions());
      if (newValue) {
        permissions.add(permission);
      } else {
        permissions.remove(permission);
      }
      try {
        setUser(userManager.updateUser(user, permissions));
      } catch (SQLException e) {
        log.severe("Error updating permissions: " + e);
        activeToggle.setSelected(oldValue);
      }
    });
  }

}
