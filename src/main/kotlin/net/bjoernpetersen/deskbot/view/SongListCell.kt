package net.bjoernpetersen.deskbot.view

import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.GridPane
import net.bjoernpetersen.deskbot.impl.effectiveAlbumArtUrl
import net.bjoernpetersen.deskbot.impl.toDurationString
import net.bjoernpetersen.musicbot.api.player.Song

class SongListCell : ListCell<Song>(), Controller {
    @FXML
    override lateinit var root: GridPane
        private set

    @FXML
    private lateinit var albumArt: ImageView
    @FXML
    private lateinit var title: Label
    @FXML
    private lateinit var description: Label
    @FXML
    private lateinit var duration: Label

    private var showImages = true

    override fun initialize() {
        text = null
        graphic = null
    }

    override fun updateItem(song: Song?, isEmpty: Boolean) {
        super.updateItem(song, isEmpty)
        if (isEmpty || song == null) {
            text = null
            graphic = null
        } else {
            applyInfo(song)
            graphic = root
        }
    }

    fun removeAlbumArt() {
        showImages = false
        root.children.remove(albumArt)
    }

    private fun applyInfo(song: Song) {
        if (showImages) albumArt.image = song.effectiveAlbumArtUrl?.let { Image(it, true) }
        title.text = song.title
        description.text = song.description.substringBefore('\n')
        duration.text = song.duration?.toDurationString()
    }
}
