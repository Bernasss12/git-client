package util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ByteArrayUtilsTest {

    @Test
    fun `test takeRange from bytearray`() {
        val original = byteArrayOf(65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90)

        val taken = original.takeRange(7, 7)
        assertEquals(7, taken.size)
        assertEquals(72, taken.first())
        assertEquals(78, taken.last())
    }

    @ParameterizedTest
    @MethodSource("provideInputData")
    fun `test big endian from bytearray`(data: Triple<ByteArray, Byte?, Int>) {
        assertEquals(data.second, data.first.toBEInt())
    }

    @ParameterizedTest
    @MethodSource("provideInputData")
    fun `test little endian from bytearray`(data: Triple<ByteArray, Byte?, Int>) {
        assertEquals(data.third, data.first.toLEInt())
    }

    companion object {
        @JvmStatic
        fun provideInputData(): List<Triple<ByteArray, Int, Int>> {
            return listOf(
                Triple(byteArrayOf(0x01), 1, 1),
                Triple(byteArrayOf(0x00, 0x01), 1, 256),
                Triple(byteArrayOf(0x01, 0x00), 256, 1),
                Triple(byteArrayOf(0x00, 0x00, 0x01), 1, 65536),
                Triple(byteArrayOf(0x01, 0x00, 0x00), 65536, 1),
                Triple(byteArrayOf(0x00, 0x01, 0x00), 256, 256),
                Triple(byteArrayOf(0x01, 0x00, 0x00, 0x00), 16777216, 1),
                Triple(byteArrayOf(0x00, 0x00, 0x00, 0x01), 1, 16777216),
                Triple(byteArrayOf(0x00, 0x00, 0x01, 0x00), 256, 65536),
                Triple(byteArrayOf(0x01, 0x00, 0x01, 0x00), 16777472, 65537)
            )
        }
    }
}