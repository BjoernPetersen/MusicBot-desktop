package net.bjoernpetersen.deskbot.view

import javafx.scene.layout.Region
import javafx.stage.Stage

interface Controller {
    val root: Region

    fun initialize()
    fun getWindowTitle(): String? = null
    fun onStageAttach(stage: Stage) = Unit
}
