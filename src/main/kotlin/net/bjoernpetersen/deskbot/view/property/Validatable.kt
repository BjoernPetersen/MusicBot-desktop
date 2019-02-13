package net.bjoernpetersen.deskbot.view.property

import javafx.scene.control.Control
import org.controlsfx.property.editor.PropertyEditor

interface Validatable<T> : PropertyEditor<T> {
    val control: Control
}
