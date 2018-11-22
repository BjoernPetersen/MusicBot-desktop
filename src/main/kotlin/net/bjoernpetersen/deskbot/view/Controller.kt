package net.bjoernpetersen.deskbot.view

import javafx.scene.layout.Region

interface Controller {
    val root: Region

    fun initialize()
}
