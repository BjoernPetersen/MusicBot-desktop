package com.github.bjoernpetersen.deskbot.model

import com.github.bjoernpetersen.jmusicbot.AdminPlugin
import com.github.bjoernpetersen.jmusicbot.Plugin
import com.github.bjoernpetersen.jmusicbot.config.Config
import com.github.bjoernpetersen.jmusicbot.provider.*
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class ObservableProviderWrapper(config: Config, provider: Provider) :
    DefaultProviderWrapper(provider) {

  private val activeEntry: Config.BooleanEntry

  val active: BooleanProperty
    @JvmName("activeProperty")
    get
  val observableConfigEntries: ObservableList<Config.Entry>

  init {
    activeEntry = config.BooleanEntry(
        provider.javaClass,
        "enable",
        "Enables plugin: ${provider.readableName}",
        false
    )
    active = SimpleBooleanProperty(activeEntry.value)
    observableConfigEntries = FXCollections.observableArrayList();
    active.addListener { _, oldValue, newValue ->
      if (oldValue != newValue) {
        if (newValue) initializeConfigEntries(config)
        else destructConfigEntries()
        activeEntry.set(newValue)
      }
    }
    addStateListener { o, n ->
      if (o === Plugin.State.INACTIVE && n === Plugin.State.CONFIG) {
        active.set(true)
        activeEntry.set(true)
        observableConfigEntries.addAll(configEntries)
      } else if (o === Plugin.State.CONFIG && n === Plugin.State.INACTIVE) {
        active.set(false)
        activeEntry.set(false)
        observableConfigEntries.clear()
      }
    }
    if (active.get()) initializeConfigEntries(config)
  }
}

class ObservableSuggesterWrapper(config: Config, suggester: Suggester) :
    DefaultSuggesterWrapper(suggester) {

  private val activeEntry: Config.BooleanEntry

  val active: BooleanProperty
    @JvmName("activeProperty")
    get
  val observableConfigEntries: ObservableList<Config.Entry>

  init {
    activeEntry = config.BooleanEntry(
        suggester.javaClass,
        "enable",
        "Enables plugin: ${suggester.readableName}",
        false
    )
    active = SimpleBooleanProperty()
    observableConfigEntries = FXCollections.observableArrayList();
    active.addListener { _, oldValue, newValue ->
      if (oldValue != newValue) {
        if (newValue) initializeConfigEntries(config)
        else destructConfigEntries()
      }
    }
    addStateListener { o, n ->
      if (o === Plugin.State.INACTIVE && n === Plugin.State.CONFIG) {
        active.set(true)
        activeEntry.set(true)
        observableConfigEntries.addAll(configEntries)
      } else if (o === Plugin.State.CONFIG && n === Plugin.State.INACTIVE) {
        active.set(false)
        activeEntry.set(false)
        observableConfigEntries.clear()
      }
    }
    active.set(activeEntry.value)
  }
}

class ObservableAdminWrapper(config: Config, admin: AdminPlugin) :
    DefaultPluginWrapper<AdminPlugin>(admin) {

  private val activeEntry: Config.BooleanEntry

  val active: BooleanProperty
    @JvmName("activeProperty")
    get
  val observableConfigEntries: ObservableList<Config.Entry>

  init {
    activeEntry = config.BooleanEntry(
        admin.javaClass,
        "enable",
        "Enables plugin: ${admin.readableName}",
        false
    )
    active = SimpleBooleanProperty()
    observableConfigEntries = FXCollections.observableArrayList();
    active.addListener { _, oldValue, newValue ->
      if (oldValue != newValue) {
        if (newValue) initializeConfigEntries(config)
        else destructConfigEntries()
      }
    }
    addStateListener { o, n ->
      if (o === Plugin.State.INACTIVE && n === Plugin.State.CONFIG) {
        active.set(true)
        activeEntry.set(true)
        observableConfigEntries.addAll(configEntries)
      } else if (o === Plugin.State.CONFIG && n === Plugin.State.INACTIVE) {
        active.set(false)
        activeEntry.set(false)
        observableConfigEntries.clear()
      }
    }
    active.set(activeEntry.value)
  }
}
