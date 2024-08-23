package util

import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test

class UtilKtTest {
    @Test
    fun splitTest() {
        val original = listOf(
            "one",
            "two",
            "three",
            "",
            "four",
            "five",
        )
        val first = listOf(
            "one",
            "two",
            "three",
        )
        val second = listOf(
            "four",
            "five",
        )

        val (firstResult, secondResult) = original.split { it.isEmpty() }

        assertIterableEquals(first, firstResult)
        assertIterableEquals(second, secondResult)
    }
}