package net.bjoernpetersen.deskbot.view

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.Separator
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.Region
import net.bjoernpetersen.musicbot.api.plugin.PluginId

class PluginOrderListCell : ListCell<PluginOrderItem>(), Controller {
    @FXML
    override lateinit var root: Region
        private set

    @FXML
    private lateinit var topLine: Separator
    @FXML
    private lateinit var bottomLine: Separator

    @FXML
    private lateinit var displayName: Label
    @FXML
    private lateinit var availability: Label

    var dragHandler: ((fromIndex: Int, toIndex: Int) -> Boolean)? = null

    override fun initialize() {
        text = null
        graphic = root
        displayName.textProperty().bind(createStringBinding(itemProperty()) { item?.displayName })
        availability.textProperty().bind(
            createStringBinding(itemProperty()) {
                when (item?.isAvailable) {
                    null -> null
                    true -> DeskBot.resources["pluginOrder.available"]
                    false -> DeskBot.resources["pluginOrder.unavailable"]
                }
            }
        )

        initializeDragAndDrop()
    }

    private fun initializeDragAndDrop() {
        setOnDragDetected(::onDragDetected)
        setOnDragOver(::onDragOver)
        setOnDragExited(::onDragExited)
        setOnDragDropped(::onDragDropped)
    }

    private fun onDragDetected(it: MouseEvent) {
        if (item != null && dragHandler != null) {
            val dragboard = startDragAndDrop(TransferMode.MOVE)
            dragboard.setContent(
                ClipboardContent().apply {
                    putString(index.toString())
                }
            )
        }
        it.consume()
    }

    private fun onDragOver(it: DragEvent) {
        if (it.gestureSource != this && it.dragboard.hasString()) {
            it.acceptTransferModes(TransferMode.MOVE)
            val posY = screenToLocal(it.screenX, it.screenY).y
            val height = height
            if (posY > height / 2) {
                topLine.isVisible = false
                bottomLine.isVisible = true
            } else {
                topLine.isVisible = true
                bottomLine.isVisible = false
            }
        }
        it.consume()
    }

    private fun onDragExited(it: DragEvent) {
        if (it.gestureSource != this && it.dragboard.hasString()) {
            topLine.isVisible = false
            bottomLine.isVisible = false
        }
        it.consume()
    }

    private fun onDragDropped(it: DragEvent) {
        if (it.gestureSource != this && it.dragboard.hasString()) {
            dragHandler?.let { handler ->
                val posY = screenToLocal(it.screenX, it.screenY).y
                val height = height
                val isAbove = posY < height / 2
                val fromIndex = it.dragboard.string.toInt()
                val newIndex = if (isAbove) index else index + 1
                val correctedNewIndex = if (fromIndex < index) {
                    newIndex - 1
                } else newIndex
                val success = handler(fromIndex, correctedNewIndex)
                it.isDropCompleted = success
            }
        }
        it.consume()
    }
}

class PluginOrderItem private constructor(
    val pluginId: PluginId,
    val isAvailable: Boolean
) {
    val className: String
        get() = pluginId.qualifiedName
    val displayName: String
        get() = pluginId.displayName

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PluginOrderItem) return false

        if (className != other.className) return false

        return true
    }

    override fun hashCode(): Int {
        return className.hashCode()
    }

    override fun toString(): String {
        return "$displayName, available: $isAvailable"
    }

    companion object {
        fun available(pluginId: PluginId) = PluginOrderItem(pluginId, true)
        fun unavailable(pluginId: PluginId) = PluginOrderItem(pluginId, false)
    }
}
