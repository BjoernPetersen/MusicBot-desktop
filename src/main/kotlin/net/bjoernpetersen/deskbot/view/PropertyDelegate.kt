package net.bjoernpetersen.deskbot.view

import javafx.beans.property.Property
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> property(property: Property<T>): ReadWriteProperty<Any, T?> = PropertyDelegate(property)

private class PropertyDelegate<T>(val property: Property<T>) : ReadWriteProperty<Any, T?> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T? {
        return this.property.value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        this.property.value = value
    }
}
