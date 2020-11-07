package net.bjoernpetersen.deskbot.view.property

import javafx.beans.value.ObservableValue
import net.bjoernpetersen.musicbot.api.config.Config
import org.controlsfx.control.PropertySheet
import java.util.Optional
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
sealed class ConfigEntryItem<T : Any>(
    private val type: KClass<T>,
    private val isSecret: Boolean
) : PropertySheet.Item {

    abstract val entry: Config.Entry<T>

    override fun getName(): String = entry.key
    override fun getDescription(): String = entry.description
    override fun getType(): Class<*> = type.java
    override fun getValue(): Any? = entry.getWithoutDefault()
    override fun getObservableValue(): Optional<ObservableValue<out Any>> = Optional.empty()
    override fun getCategory(): String = if (isSecret) "Secret" else "Plaintext"

    companion object {
        fun forEntry(entry: Config.Entry<*>, secret: Boolean): ConfigEntryItem<*> {
            return when (entry) {
                is Config.StringEntry -> StringItem(entry, secret)
                is Config.BooleanEntry -> BooleanItem(entry, secret)
                is Config.SerializedEntry -> SerializedItem(
                    entry as Config.SerializedEntry<Any>, secret
                )
                else -> throw IllegalArgumentException(
                    "Unknown Config.Entry type: ${entry::class.qualifiedName}"
                )
            }
        }
    }
}

private class StringItem(
    override val entry: Config.StringEntry,
    isSecret: Boolean
) : ConfigEntryItem<String>(String::class, isSecret) {

    override fun setValue(value: Any?) {
        entry.set(value as String?)
    }
}

private class BooleanItem(
    override val entry: Config.BooleanEntry,
    isSecret: Boolean
) : ConfigEntryItem<Boolean>(Boolean::class, isSecret) {

    override fun setValue(value: Any?) {
        entry.set(value as? Boolean)
    }

    override fun getValue(): Any? {
        return entry.get()
    }
}

@Suppress("UNCHECKED_CAST")
private class SerializedItem(
    override val entry: Config.SerializedEntry<Any>,
    isSecret: Boolean
) : ConfigEntryItem<Any>(Any::class, isSecret) {

    override fun setValue(value: Any?) {
        entry.set(value)
    }
}
