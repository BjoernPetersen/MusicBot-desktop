package net.bjoernpetersen.deskbot.view

import javafx.concurrent.Task
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.util.StringConverter
import java.util.ResourceBundle

val Controller.stage: Stage
    get() = this.root.scene.window as Stage

fun Controller.closeWindow() {
    (this as? Stage)?.close() ?: IllegalStateException()
}

fun Parent.show(wait: Boolean = false, modal: Boolean = false, title: String = ""): Stage {
    return Stage().also {
        it.scene = Scene(this)
        if (title.isNotBlank()) it.title = title
        if (modal) it.initModality(Modality.APPLICATION_MODAL)
        if (wait) it.showAndWait()
        else it.show()
    }
}

fun Controller.replaceWindow(parent: Parent) {
    parent.show()
    stage.close()
}

fun <T> stringConverter(toString: (T?) -> String?) = object : StringConverter<T>() {
    override fun toString(`object`: T?): String? {
        return toString(`object`)
    }

    override fun fromString(string: String?): T? {
        return null
    }
}

fun <T> task(action: Task<T>.() -> T): Task<T> = object : Task<T>() {
    override fun call(): T = this.action()
}

operator fun ResourceBundle.get(key: String): String = getString(key)
