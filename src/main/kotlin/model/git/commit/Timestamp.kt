package model.git.commit

import java.time.Instant
import java.util.*
import kotlin.math.absoluteValue
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class Timestamp(
    val epochSeconds: Instant,
    val timezoneOffset: Duration,
) {
    companion object {
        fun now(): Timestamp {
            return Timestamp(
                epochSeconds = Instant.now(),
                timezoneOffset = TimeZone.getDefault().rawOffset.milliseconds,
            )
        }

        fun fromString(string: String): Timestamp {
            string.split(" ").let {
                return Timestamp(
                    epochSeconds = Instant.ofEpochSecond(it[0].toLong()),
                    timezoneOffset = it[1].toOffsetSeconds(),
                )
            }
        }

        fun String.toOffsetSeconds(): Duration {
            val sign = if (this[0] == '-') -1 else 1
            val hours = this.slice(1..2).toInt()
            val minutes = this.slice(3..4).toInt()

            return (sign * (hours.hours.inWholeSeconds + minutes.minutes.inWholeSeconds)).seconds
        }

        fun Duration.toOffsetString(): String {
            val sign = if (this.isNegative()) '-' else '+'
            val hours = inWholeHours.absoluteValue
            val minutes = inWholeMinutes.mod(60).absoluteValue

            return "%c%02d%02d".format(sign, hours, minutes)
        }
    }

    fun toTimestampString() = buildString {
        append(epochSeconds.epochSecond)
        append(" ")
        append(timezoneOffset.toOffsetString())
    }
}