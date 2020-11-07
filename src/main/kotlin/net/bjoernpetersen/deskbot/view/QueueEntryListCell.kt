package net.bjoernpetersen.deskbot.view

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.Separator
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.input.ClipboardContent
import javafx.scene.input.DragEvent
import javafx.scene.input.MouseEvent
import javafx.scene.input.TransferMode
import javafx.scene.layout.GridPane
import net.bjoernpetersen.deskbot.impl.localAlbumArtUrl
import net.bjoernpetersen.deskbot.impl.toDurationString
import net.bjoernpetersen.musicbot.api.player.QueueEntry

class QueueEntryListCell : ListCell<QueueEntry>(), Controller {
    @FXML
    override lateinit var root: GridPane
        private set

    @FXML
    private lateinit var topLine: Separator
    @FXML
    private lateinit var bottomLine: Separator

    @FXML
    private lateinit var albumArt: ImageView
    @FXML
    private lateinit var title: Label
    @FXML
    private lateinit var description: Label
    @FXML
    private lateinit var user: Label
    @FXML
    private lateinit var duration: Label

    private var showImages: Boolean = true
    var dragHandler: ((fromIndex: Int, toIndex: Int) -> Boolean)? = null

    override fun initialize() {
        text = null
        graphic = null
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

    override fun updateItem(entry: QueueEntry?, isEmpty: Boolean) {
        super.updateItem(entry, isEmpty)
        if (isEmpty || entry == null) {
            text = null
            graphic = null
        } else {
            applyInfo(entry)
            graphic = root
        }
    }

    fun removeAlbumArt() {
        showImages = false
        root.children.remove(albumArt)
    }

    private fun applyInfo(entry: QueueEntry) {
        val song = entry.song
        if (showImages) albumArt.image = song.localAlbumArtUrl?.let { Image(it, true) }
        title.text = song.title
        description.text = song.description.substringBefore('\n')
        duration.text = song.duration?.toDurationString()
        user.text = entry.user.name
    }
}
