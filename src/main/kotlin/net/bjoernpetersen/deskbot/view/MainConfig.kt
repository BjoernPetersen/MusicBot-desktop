package net.bjoernpetersen.deskbot.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.scene.layout.Region
import net.bjoernpetersen.deskbot.lifecycle.Lifecyclist
import net.bjoernpetersen.deskbot.view.property.ConfigEntryEditorFactory
import net.bjoernpetersen.deskbot.view.property.ConfigEntryItem
import org.controlsfx.control.PropertySheet

class MainConfig : Controller {
    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var propertySheet: PropertySheet

    val lifecycleProperty: ObjectProperty<Lifecyclist> = SimpleObjectProperty()
    var lifecycle by property(lifecycleProperty)

    @FXML
    override fun initialize() {
        propertySheet.propertyEditorFactory = ConfigEntryEditorFactory()
        lifecycleProperty.addListener { _, _, lifecycle ->
            propertySheet.items.clear()
            if (lifecycle != null) {
                propertySheet.items.addAll(
                    lifecycle.getMainConfig().allPlain
                        .map { ConfigEntryItem.forEntry(it, false) }
                )

                propertySheet.items.addAll(
                    lifecycle.getMainConfig().allSecret
                        .map { ConfigEntryItem.forEntry(it, true) }
                )
            }
        }
    }
}
