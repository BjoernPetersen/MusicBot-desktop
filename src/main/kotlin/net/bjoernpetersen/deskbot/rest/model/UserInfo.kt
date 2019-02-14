package net.bjoernpetersen.deskbot.rest.model

import net.bjoernpetersen.musicbot.api.auth.User

data class UserInfo(
    private val name: String,
    private val permissions: List<String>) {
}

fun User.toInfo(): UserInfo = UserInfo(name, permissions.map { it.label })
