package com.github.bjoernpetersen.deskbot.model

import com.github.bjoernpetersen.jmusicbot.*
import com.github.bjoernpetersen.jmusicbot.config.Config
import com.github.bjoernpetersen.jmusicbot.playback.PlaybackFactory
import com.github.bjoernpetersen.jmusicbot.provider.Provider
import com.github.bjoernpetersen.jmusicbot.provider.Suggester
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

interface ObservablePluginWrapper<out P : Plugin> : PluginWrapper<P> {
  val enabledEntry: Config.BooleanEntry

  fun enabledProperty(): BooleanProperty
  var isEnabled: Boolean
    get() = enabledProperty().get()
    set(value) = enabledProperty().set(value)

  val observableConfigEntries: ObservableList<Config.Entry>
}

private fun init(config: Config, wrapper: ObservablePluginWrapper<*>) {
  wrapper.enabledProperty().addListener { _, old, new ->
    if (old != new) {
      if (new) wrapper.initializeConfigEntries(config)
      else wrapper.destructConfigEntries()
      wrapper.enabledEntry.set(new)
    }
  }
  wrapper.addStateListener { old, new ->
    if (old === Plugin.State.INACTIVE && new === Plugin.State.CONFIG) {
      wrapper.observableConfigEntries.addAll(wrapper.configEntries)
    } else if (old === Plugin.State.CONFIG && new === Plugin.State.INACTIVE) {
      wrapper.observableConfigEntries.clear()
    }
  }
  wrapper.isEnabled = wrapper.enabledEntry.value
}

class ObservablePlaybackFactoryWrapper(config: Config, factory: PlaybackFactory) :
    DefaultPlaybackFactoryWrapper(factory), ObservablePluginWrapper<PlaybackFactory> {

  override val enabledEntry: Config.BooleanEntry = config.BooleanEntry(factory.javaClass, "enable", "", false)
  private val enabledProperty: BooleanProperty = SimpleBooleanProperty(false)
  override fun enabledProperty() = enabledProperty

  override val observableConfigEntries: ObservableList<Config.Entry> = FXCollections.observableArrayList()

  init {
    init(config, this)
  }
}

class ObservableProviderWrapper(config: Config, provider: Provider) : DefaultProviderWrapper(provider),
    ObservablePluginWrapper<Provider> {

  override val enabledEntry: Config.BooleanEntry = config.BooleanEntry(provider.javaClass, "enable", "", false)
  private val enabledProperty: BooleanProperty = SimpleBooleanProperty(false)
  override fun enabledProperty() = enabledProperty

  override val observableConfigEntries: ObservableList<Config.Entry> = FXCollections.observableArrayList()

  init {
    init(config, this)
  }
}

class ObservableSuggesterWrapper(config: Config, suggester: Suggester) : DefaultSuggesterWrapper(suggester),
    ObservablePluginWrapper<Suggester> {

  override val enabledEntry: Config.BooleanEntry = config.BooleanEntry(suggester.javaClass, "enable", "", false)
  private val enabledProperty: BooleanProperty = SimpleBooleanProperty(false)
  override fun enabledProperty() = enabledProperty

  override val observableConfigEntries: ObservableList<Config.Entry> = FXCollections.observableArrayList()

  init {
    init(config, this)
  }
}

class ObservableAdminPluginWrapper(config: Config, plugin: AdminPlugin) :
    DefaultAdminPluginWrapper(plugin), ObservablePluginWrapper<AdminPlugin> {

  override val enabledEntry: Config.BooleanEntry = config.BooleanEntry(plugin.javaClass, "enable", "", false)
  private val enabledProperty: BooleanProperty = SimpleBooleanProperty(false)
  override fun enabledProperty() = enabledProperty

  override val observableConfigEntries: ObservableList<Config.Entry> = FXCollections.observableArrayList()

  init {
    init(config, this)
  }
}
