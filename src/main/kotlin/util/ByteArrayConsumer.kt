package util

class ByteArrayConsumer(private var bytes: ByteArray) {
    fun consumeUntil(byte: Byte): ByteArray {
        System.err.println("Start: ${String(bytes)}")
        val index = bytes.indexOf(byte)
        val result = consume(index)
        System.err.println("Result: ${String(result)}")
        erase(index + 1)
        System.err.println("End: ${String(bytes)}")
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

    fun hasAfter(lenght: Int, byte: Byte): Boolean {
        return bytes.indexOf(byte) + lenght >= bytes.size
    }

    fun hasNext(byte: Byte): Boolean {
        return bytes.indexOf(byte) != -1
    }

    fun hasMore(lenght: Int): Boolean {
        return bytes.size >= lenght
    }
}