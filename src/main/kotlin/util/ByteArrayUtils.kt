package util

import java.nio.charset.Charset

inline fun ByteArray.consumeUntil(
    predicate: (Byte) -> Boolean
): ByteArray {
    val iterator = iterator()
    var array = byteArrayOf()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (predicate(next)) break
        array += next
    }
    return array
}

inline fun ByteArray.takeLastUntil(
    predicate: (Byte) -> Boolean
): ByteArray {
    val iterator = reversedArray().iterator()
    var array = byteArrayOf()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (predicate(next)) break
        array += next
    }
    return array.reversedArray()
}

fun ByteArray.takeAsByteArray(count: Int) = take(count).toByteArray()
fun ByteArray.takeLastAsByteArray(count: Int) = takeLast(count).toByteArray()
fun ByteArray.dropAsByteArray(count: Int) = drop(count).toByteArray()
fun ByteArray.dropLastAsByteArray(count: Int) = dropLast(count).toByteArray()

fun ByteArray.asString(charset: Charset = Charsets.UTF_8) = String(this, charset)

fun buildByteArray(block: ByteArrayBuilder.() -> Unit): ByteArray {
    val bytes = ByteArrayBuilder()
    block.invoke(bytes)
    return bytes.build()
}

fun ByteArray.split(char: Char): List<ByteArray> = this.split { it == char.code.toByte() }

fun ByteArray.split(predicate: (Byte) -> Boolean): List<ByteArray> {
    val iterator = iterator()
    val list = mutableListOf<ByteArray>()
    var bytes = byteArrayOf()
    while (iterator.hasNext()) {
        val current = iterator.next()
        if (predicate(current)) {
            list.add(bytes.copyOf())
            bytes = byteArrayOf()
        } else {
            bytes += current
        }
    }
    return list.toList()
}

fun ByteArray.splitInTwo(separator: Byte) = splitInTwo { it == separator }
fun ByteArray.splitInTwo(predicate: (Byte) -> Boolean): Pair<ByteArray, ByteArray> {
    val iterator = iterator()
    val first = ByteArrayBuilder()
    val second = ByteArrayBuilder()
    var triggered = false
    while (iterator.hasNext()) {
        val current = iterator.next()
        if (!triggered && predicate(current)) {
            triggered = true
        } else {
            if (triggered) {
                second.appendByte(current)
            } else {
                first.appendByte(current)
            }
        }
    }
    return first.build() to second.build()
}

fun ByteArray.toInt(): Int {
    require(this.size == 4) { "This method is supposed to turn a 4 byte array to an Int" }
    return String(this).toInt(16)
}

fun ByteArray.toIntRaw(): Int {
    require(this.size == 4) { "This method is supposed to turn a 4 byte array to an Int" }
    var result = 0
    for (byte in this) {
        result = (result shl 8) or (byte.toInt() and 0xFF)
    }
    return result
}