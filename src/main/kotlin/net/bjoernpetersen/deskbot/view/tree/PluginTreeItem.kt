package net.bjoernpetersen.deskbot.view.tree

import com.google.common.collect.Multimap
import com.google.common.collect.MultimapBuilder
import javafx.collections.ObservableList
import javafx.scene.control.TreeItem
import net.bjoernpetersen.musicbot.api.plugin.management.findDependencies
import net.bjoernpetersen.musicbot.spi.plugin.Plugin
import net.bjoernpetersen.musicbot.spi.plugin.management.DependencyManager
import kotlin.reflect.KClass

@Suppress("UnstableApiUsage")
class PluginTreeItem(
    private val dependencyManager: DependencyManager,
    val base: KClass<out Plugin>,
    plugin: Plugin? = null,
    private val itemsByBase: Multimap<KClass<out Plugin>, PluginTreeItem> =
        MultimapBuilder.hashKeys().arrayListValues().build<KClass<out Plugin>, PluginTreeItem>()
) :
    TreeItem<Plugin>(plugin) {

    private var firstChildren = true

    init {
        itemsByBase[base].forEach { it.valueProperty().bindBidirectional(valueProperty()) }
        itemsByBase.put(base, this)
        valueProperty().addListener { _, _, value ->
            children.clear()
            if (value != null) compute()?.let {
                children.addAll(it)
            }
        }
    }

    private fun compute() = value?.findDependencies()
        ?.map {
            PluginTreeItem(dependencyManager, it, dependencyManager.getDefault(it), itemsByBase)
        }
        ?.map { it as TreeItem<Plugin> }

    override fun isLeaf(): Boolean = value?.findDependencies()?.isEmpty() ?: true
    override fun getChildren(): ObservableList<TreeItem<Plugin>> {
        if (firstChildren) {
            firstChildren = false
            compute()?.let { super.getChildren().addAll(it) }
        }
        return super.getChildren()
    }

    override fun toString(): String {
        return "PluginTreeItem(base=$base, value=$value)"
    }
}
