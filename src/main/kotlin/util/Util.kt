@file:OptIn(ExperimentalStdlibApi::class)
@file:Suppress("unused")

package util

import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

val RESET = "\u001B[0m"
val BLACK = "\u001B[30m"
val RED = "\u001B[31m"
val GREEN = "\u001B[32m"
val YELLOW = "\u001B[33m"
val BLUE = "\u001B[34m"
val PURPLE = "\u001B[35m"
val CYAN = "\u001B[36m"
val WHITE = "\u001B[37m"

fun String.black() = "$BLACK$this$RESET"
fun String.red() = "$RED$this$RESET"
fun String.green() = "$GREEN$this$RESET"
fun String.yellow() = "$YELLOW$this$RESET"
fun String.blue() = "$BLUE$this$RESET"
fun String.purple() = "$PURPLE$this$RESET"
fun String.cyan() = "$CYAN$this$RESET"
fun String.white() = "$WHITE$this$RESET"
fun String.black(applyColor: Boolean) = if (applyColor) this.black() else this
fun String.red(applyColor: Boolean) = if (applyColor) this.red() else this
fun String.green(applyColor: Boolean) = if (applyColor) this.green() else this
fun String.yellow(applyColor: Boolean) = if (applyColor) this.yellow() else this
fun String.blue(applyColor: Boolean) = if (applyColor) this.blue() else this
fun String.purple(applyColor: Boolean) = if (applyColor) this.purple() else this
fun String.cyan(applyColor: Boolean) = if (applyColor) this.cyan() else this
fun String.white(applyColor: Boolean) = if (applyColor) this.white() else this

fun String.color(color: String) = "$color$this$RESET"
fun String.color(color: String, applyColor: Boolean) = if (applyColor) color(color) else this

val ANSI_PATTERN = "\u001B\\[[;\\d]*m".toRegex()

fun String.stripANSI() = this.replace(ANSI_PATTERN, "")

val String.vlength: Int
    get() = stripANSI().length

fun String.padEndVisible(length: Int, char: Char = ' ') =
    padEnd(length + this.length - this.vlength, char)

fun <T> List<T>.split(predicate: (T) -> Boolean): Pair<List<T>, List<T>> {
    return take(indexOfFirst(predicate)) to drop(indexOfFirst(predicate).inc())
}

fun printByteArrayComparison(expected: ByteArray, check: ByteArray) {
    fun top(lenght: Int) = "╭" + "─".repeat(max(0, lenght)) + "┬"
    fun bottom(lenght: Int) = "╰" + "─".repeat(max(0, lenght)) + "┴"
    fun divider(lenght: Int) = "├".padEnd(max(0, lenght + 1), '─') + "┤"
    fun empty(lenght: Int) = "│" + " ".repeat(max(0, lenght)) + "│"
    fun text(lenght: Int, text: String): String = buildString {
        append("│")
        append(" ".repeat(max(1.0, floor((lenght - text.vlength) / 2.0)).toInt()))
        append(text)
        append(" ".repeat(max(1.0, ceil((lenght - text.vlength) / 2.0)).toInt()))
        append("│")
    }


    val size = max(expected.size, check.size)
    val paddingByte: Byte = 1

    val output = check.pad(size, paddingByte).zip(expected.pad(size, paddingByte)).map {
        Triple(it.first, if (it.first == it.second) 1 else if (it.first == paddingByte) 3 else if(it.second == paddingByte) 2 else 0, it.second) //  FIXME
    }

    val mistakes = output.count { it.second == 0 }

    var prefixDivider = if (mistakes > 0) {
        text(0, "Diffs: $mistakes").red()
    } else {
        text(0, "Matching".green())
    }
    var prefixTwo = text(0, "Actual")
    var prefixExpected = text(0, "Expected")

    val length = listOf(prefixExpected.vlength, prefixTwo.vlength, prefixDivider.vlength).max()

    prefixDivider = if (mistakes > 0) {
        text(length, "Diffs: $mistakes".red())
    } else {
        text(length, "Matching".green())
    }
    prefixTwo = text(length, "Actual")
    prefixExpected = text(length, "Expected")

    val specialCharacters: String.() -> String = {
        when {
            equals("\n") -> "↵".blue()
            equals("\u0000") -> "␀".blue()
            equals(" ") -> "⎵".yellow()
            equals("\r") -> "⇤".yellow()

            first().code in 9 .. 0xd3 -> this
            else -> "☒".cyan()
        }
    }

    val individualTransform: (Byte, Int) -> String = { value, mode -> value.toInt().toChar().toString().specialCharacters().padEndVisible(2).red(mode == 0).black(mode == 2 ) }

    val above = output.joinToString(prefix = top(length), postfix = "╮", separator = "┬") { "──" }
    val before = output.joinToString(prefix = prefixTwo, postfix = "│", separator = "│") { individualTransform(it.first, it.second) }
    val first = output.joinToString(prefix = empty(length), postfix = "│", separator = "│") { it.first.toHexString().red(it.second == 0).black(it.second == 2 ) }
    val second = output.joinToString(prefix = prefixDivider, postfix = "│", separator = "│") { if (it.second == 0) "↑↓".red() else if (it.second == 2) "↑↑".black() else if(it.second == 3) "↓↓".black() else "  " }
    val third = output.joinToString(prefix = empty(length), postfix = "│", separator = "│") { it.third.toHexString().red(it.second == 0).black(it.second == 3 ) }
    val after = output.joinToString(prefix = prefixExpected, postfix = "│", separator = "│") { individualTransform(it.third, it.second) }
    val below = output.joinToString(prefix = bottom(length), postfix = "╯", separator = "┴") { "──" }

    println(above)
    println(before)
    println(first)
    println(second)
    println(third)
    println(after)
    println(below)
}

inline fun <T, R> T.then(block: (T) -> R): R {
    return block(this)
}