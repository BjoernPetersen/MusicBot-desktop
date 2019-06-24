package net.bjoernpetersen.deskbot.impl

import kotlin.random.Random

fun randomString(length: Int): String {
    val rand = Random.Default
    val sb = StringBuilder(length)
    (1..length)
        .map { rand.nextInt(97, 123) }
        .map { it.toChar() }
        .forEach { sb.append(it) }
    return sb.toString()
}

fun Int.toDurationString(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%d:%02d".format(minutes, seconds)
}
