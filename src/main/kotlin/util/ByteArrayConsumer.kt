package util

class ByteArrayConsumer(private var bytes: ByteArray) {
    fun consumeUntil(byte: Byte): ByteArray {
        val index = bytes.indexOf(byte)
        val result = consume(index)
        erase(index + 1)
        return result
    }

    fun consume(count: Int): ByteArray {
        val result = get(count)
        erase(count)
        return result
    }

    private fun get(count: Int) = bytes.take(count).toByteArray()
    private fun erase(count: Int) {
        bytes = bytes.drop(count).toByteArray()
    }

    fun consumeUntilAfter(lenght: Int, byte: Byte): ByteArray {
        return consume(bytes.indexOf(byte) + lenght + 1)
    }
    fun hasUntilAfter(lenght: Int, byte: Byte): Boolean {
        return bytes.indexOf(byte) + lenght <= bytes.size
    }

    fun hasNext(byte: Byte): Boolean {
        return bytes.indexOf(byte) != -1
    }
}