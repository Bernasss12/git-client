package model.git.commit

import java.time.Instant
import java.util.*

class Timestamp(
    val epochSeconds: Instant,
    val timezoneOffset: TimeZone,
) {
    companion object {
        fun now(): Timestamp {
            return Timestamp(
                epochSeconds = Instant.now(),
                timezoneOffset = TimeZone.getDefault(),
            )
        }

        fun fromString(string: String): Timestamp {
            string.split(" ").let {
                return Timestamp(
                    epochSeconds = Instant.ofEpochSecond(it[0].toLong()),
                    timezoneOffset = TimeZone.getTimeZone(it[1]),
                )
            }
        }
    }

    fun toTimestampString() = buildString {
        append(epochSeconds.epochSecond)
        append(" ")
        append(timezoneOffset.id.replace(":", ""))
    }
}