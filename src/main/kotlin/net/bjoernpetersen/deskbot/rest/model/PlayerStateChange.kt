package net.bjoernpetersen.deskbot.rest.model

enum class PlayerStateAction {
    PLAY, PAUSE, SKIP
}

data class PlayerStateChange(val action: PlayerStateAction)
