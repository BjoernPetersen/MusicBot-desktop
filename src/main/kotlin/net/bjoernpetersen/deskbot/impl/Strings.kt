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
