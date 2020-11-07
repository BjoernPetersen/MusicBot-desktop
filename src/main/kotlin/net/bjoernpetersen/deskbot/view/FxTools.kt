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
    stage.close()
}

fun Controller.show(wait: Boolean = false, modal: Boolean = false): Stage {
    return root.show(wait, modal, getWindowTitle() ?: "", this)
}

fun Parent.show(wait: Boolean = false, modal: Boolean = false, title: String = ""): Stage {
    return show(wait, modal, title, null)
}

private fun Parent.show(
    wait: Boolean = false,
    modal: Boolean = false,
    title: String = "",
    controller: Controller? = null
): Stage {
    return Stage().also {
        it.scene = Scene(this)
        if (title.isNotBlank()) it.title = title
        if (modal) it.initModality(Modality.APPLICATION_MODAL)
        controller?.onStageAttach(it)
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
