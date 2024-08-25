package util

import Remote.consumeVariableLenghtInteger
import model.git.ObjectType.Companion.toBinaryString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertIterableEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

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

    @ParameterizedTest
    @MethodSource("provideInputData")
    fun testConsumeVariableLenghtInteger(data: Triple<ByteArray, Byte?, Int>) {
        val consumer = ByteArrayConsumer(data.first)
        val result = consumer.consumeVariableLenghtInteger(data.second)

        val expected = data.third.toBinaryString().padStart(32, '0').toByteArray()
        val actual = result.toBinaryString().padStart(32, '0').toByteArray()

        printByteArrayComparison(expected = expected, check = actual)

        assertEquals(data.third, result)
    }

    companion object {
        @JvmStatic
        fun provideInputData(): List<Triple<ByteArray, Byte?, Int>> {
            return listOf(
                // Single byte encoding
                Triple(byteArrayOf(0b00000000), null, 0),
                Triple(byteArrayOf(0b01111111), null, 127),
                // Two-byte encoding
                Triple(byteArrayOf(0b10000000.toByte(), 0b00000001), null, 128),
                Triple(byteArrayOf(0b11111111.toByte(), 0b01111111), null, 16383),
                // Three-byte encoding
                Triple(byteArrayOf(0b10000000.toByte(), 0b10000000.toByte(), 0b00000001), null, 16384),
                Triple(byteArrayOf(0b11111111.toByte(), 0b11111111.toByte(), 0b01111111), null, 2097151),
                // Four-byte encoding
                Triple(byteArrayOf(0b10000000.toByte(), 0b10000000.toByte(), 0b10000000.toByte(), 0b00000001), null, 2097152),
                Triple(byteArrayOf(0b11111111.toByte(), 0b11111111.toByte(), 0b11111111.toByte(), 0b01111111), null, 268435455),
                // Five-byte encoding
                Triple(byteArrayOf(0b10000000.toByte(), 0b10000000.toByte(), 0b10000000.toByte(), 0b10000000.toByte(), 0b00000001), null, 268435456),
                Triple(byteArrayOf(0b11111111.toByte(), 0b11111111.toByte(), 0b11111111.toByte(), 0b11111111.toByte(), 0b00000111), null, 2147483647),
                // With non-null initial bytes
                Triple(byteArrayOf(), 0b00000001.toByte(), 1),
                Triple(byteArrayOf(), 0b00001001.toByte(), 9),
                Triple(byteArrayOf(), 0b00001111.toByte(), 15),
                Triple(byteArrayOf(0b00000001), 0b10000000.toByte(), 16),
                Triple(byteArrayOf(0b01111111), 0b11111000.toByte(), 2040),
                Triple(byteArrayOf(0b10000000.toByte(), 0b00001001), 0b10000010.toByte(), 18434),
                Triple(byteArrayOf(0b11111111.toByte(), 0b11110111.toByte(), 0b01010101), 0b10000011.toByte(), 22527987),
            )
        }
    }
}