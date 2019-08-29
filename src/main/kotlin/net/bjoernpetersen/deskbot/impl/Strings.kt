package net.bjoernpetersen.deskbot.impl

import kotlin.random.Random

private const val A_CHAR_CODE = 97
private const val Z_CHAR_CODE = 122

fun randomString(length: Int): String {
    val rand = Random.Default
    val sb = StringBuilder(length)
    (1..length)
        .map { rand.nextInt(A_CHAR_CODE, Z_CHAR_CODE + 1) }
        .map { it.toChar() }
        .forEach { sb.append(it) }
    return sb.toString()
}

@Suppress("MagicNumber")
fun Int.toDurationString(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%d:%02d".format(minutes, seconds)
}
