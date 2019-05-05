package net.bjoernpetersen.deskbot.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class StringsTest {

    @Test
    fun randomStringLength() {
        assertEquals(10, randomString(10).length)
    }
}
