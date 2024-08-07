package util.model

inline fun ByteArray.takeUntil(
    predicate: (Byte) -> Boolean
): ByteArray {
    val iterator = iterator()
    var array = byteArrayOf()
    while (iterator.hasNext()) {
        val next = iterator.next()
        if (!predicate(next)) break
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
        if (!predicate(next)) break
        array += next
    }
    return array.reversedArray()
}