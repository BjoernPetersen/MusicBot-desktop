package net.bjoernpetersen.deskbot.rest.model

import net.bjoernpetersen.musicbot.api.player.ErrorState
import net.bjoernpetersen.musicbot.api.player.PauseState
import net.bjoernpetersen.musicbot.api.player.PlayState
import net.bjoernpetersen.musicbot.api.player.StopState

enum class PlayerStateType {
    PLAY, PAUSE, STOP, ERROR
}

data class PlayerState(
    val state: PlayerStateType,
    val songEntry: SongEntry?)

private typealias CorePlayerState = net.bjoernpetersen.musicbot.api.player.PlayerState

fun CorePlayerState.toModel() = PlayerState(when (this) {
    is PlayState -> PlayerStateType.PLAY
    is PauseState -> PlayerStateType.PAUSE
    is StopState -> PlayerStateType.STOP
    is ErrorState -> PlayerStateType.ERROR
}, entry?.toModel())
