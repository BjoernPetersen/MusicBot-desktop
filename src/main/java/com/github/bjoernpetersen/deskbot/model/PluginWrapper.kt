package com.github.bjoernpetersen.deskbot.model

import com.github.bjoernpetersen.jmusicbot.Plugin
import com.github.bjoernpetersen.jmusicbot.config.Config
import com.github.bjoernpetersen.jmusicbot.provider.DefaultProviderWrapper
import com.github.bjoernpetersen.jmusicbot.provider.DefaultSuggesterWrapper
import com.github.bjoernpetersen.jmusicbot.provider.Provider
import com.github.bjoernpetersen.jmusicbot.provider.Suggester
import javafx.beans.property.BooleanProperty
import javafx.beans.property.SimpleBooleanProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList

class ObservableProviderWrapper(config: Config, provider: Provider) : DefaultProviderWrapper(provider) {
    private val activeEntry: Config.BooleanEntry

    val active: BooleanProperty
        @JvmName("activeProperty")
        get
    val observableConfigEntries: ObservableList<Config.Entry>

    init {
        activeEntry = config.booleanEntry(
                provider.javaClass,
                "enable",
                "Enables plugin: ${provider.readableName}",
                false
        )
        active = SimpleBooleanProperty(activeEntry.get())
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

class ObservableSuggesterWrapper(config: Config, suggester: Suggester) : DefaultSuggesterWrapper(suggester) {
    private val activeEntry: Config.BooleanEntry

    val active: BooleanProperty
        @JvmName("activeProperty")
        get
    val observableConfigEntries: ObservableList<Config.Entry>

    init {
        activeEntry = config.booleanEntry(
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
        active.set(activeEntry.get())
    }
}
