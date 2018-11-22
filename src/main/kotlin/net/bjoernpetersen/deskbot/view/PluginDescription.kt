package net.bjoernpetersen.deskbot.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.Label
import javafx.scene.layout.Border
import javafx.scene.layout.BorderStroke
import javafx.scene.layout.BorderStrokeStyle
import javafx.scene.layout.BorderWidths
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Region
import javafx.scene.paint.Color
import net.bjoernpetersen.musicbot.spi.plugin.Plugin

class PluginDescription : Controller {
    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var nameLabel: Label
    @FXML
    private lateinit var idLabel: Label
    @FXML
    private lateinit var descriptionLabel: Label

    val pluginProperty: ObjectProperty<Plugin> = SimpleObjectProperty()
    var plugin by property(pluginProperty)

    @FXML
    override fun initialize() {
        BorderStroke(Color.GRAY, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)
            .let { root.border = Border(it, it, it, it) }
        root.padding = Insets(5.0)

        nameLabel.textProperty()
            .bind(createStringBinding(pluginProperty) { plugin?.name })
        idLabel.textProperty()
            .bind(createStringBinding(pluginProperty) { plugin?.let { it::class.qualifiedName } })
        descriptionLabel.textProperty()
            .bind(createStringBinding(pluginProperty) { plugin?.description })
    }
}
