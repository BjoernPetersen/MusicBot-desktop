package net.bjoernpetersen.deskbot.view.property

import javafx.scene.control.Control
import org.controlsfx.property.editor.PropertyEditor

class ValidatableWrapper<T>(
    propertyEditor: PropertyEditor<T>
) : PropertyEditor<T> by propertyEditor, Validatable<T> {

    override val control: Control = propertyEditor.editor as Control
}
