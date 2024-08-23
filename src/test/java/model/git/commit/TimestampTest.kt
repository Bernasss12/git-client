package model.git.commit

import model.git.commit.Timestamp.Companion.toOffsetSeconds
import model.git.commit.Timestamp.Companion.toOffsetString
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.time.Duration.Companion.seconds

class TimestampTest {

    @Test
    fun testTimestamp() {

    }


    @ParameterizedTest
    @MethodSource("provideInputData")
    fun testConvertTimeOffsetToString(data: Pair<String, Int>) {
        assertEquals(data.first, data.second.seconds.toOffsetString())
    }

    @ParameterizedTest
    @MethodSource("provideInputData")
    fun testConvertStringToTimeOffset(data: Pair<String, Int>) {
        assertEquals(data.second, data.first.toOffsetSeconds().inWholeSeconds)
    }

    companion object {
        @JvmStatic
        fun provideInputData() = listOf(
            "+1200" to 43200L,
            "-0500" to -18000L,
            "+0230" to 9000L,
            "-0030" to -1800L,
            "+0000" to 0L,
        )
    }
}