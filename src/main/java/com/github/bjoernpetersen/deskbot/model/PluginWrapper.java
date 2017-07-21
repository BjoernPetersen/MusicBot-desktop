package com.github.bjoernpetersen.deskbot.model;

import com.github.bjoernpetersen.jmusicbot.IdPlugin;
import com.github.bjoernpetersen.jmusicbot.NamedPlugin;
import com.github.bjoernpetersen.jmusicbot.ProviderManager;
import com.github.bjoernpetersen.jmusicbot.ProviderManager.State;
import com.github.bjoernpetersen.jmusicbot.config.Config;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.annotation.Nonnull;

public class PluginWrapper<P extends IdPlugin> {

  @Nonnull
  private static final Logger log = Logger.getLogger(PluginWrapper.class.getName());

  @Nonnull
  private final ProviderManager manager;
  @Nonnull
  private final Config.BooleanEntry activeConfig;
  @Nonnull
  private P plugin;
  @Nonnull
  private final BooleanProperty active;
  @Nonnull
  private final ObservableList<Config.Entry> entries;

  public PluginWrapper(@Nonnull Config config, @Nonnull ProviderManager providerManager,
      @Nonnull P plugin) {
    this.manager = providerManager;
    this.activeConfig = config.booleanEntry(
        plugin.getClass(),
        "enable",
        "Enables plugin: " + plugin.getReadableName(),
        false
    );

    this.plugin = plugin;
    this.active = new SimpleBooleanProperty(false);
    this.entries = FXCollections.observableArrayList();

    this.manager.addStateListener(getPlugin(), (old, state) -> {
      if (old == State.INACTIVE && state != State.INACTIVE) {
        active.set(true);
      }
    });
    this.active.addListener(((observable, oldValue, newValue) -> {
      activeConfig.set(newValue);
      entries.clear();
      if (newValue) {
        entries.addAll(manager.getConfigEntries(getPlugin()));
      } else {
        manager.destructConfigEntries(getPlugin());
      }
    }));
    this.active.set(activeConfig.get());
  }

  public ObservableList<? extends Config.Entry> getConfigEntries() {
    return this.entries;
  }

  public void setActive(boolean active) {
    activeProperty().set(active);
  }

  public boolean isActive() {
    return activeProperty().get();
  }

  @Nonnull
  public BooleanProperty activeProperty() {
    return active;
  }

  @Nonnull
  public P getPlugin() {
    return this.plugin;
  }
}
