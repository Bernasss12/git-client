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
        bytes = bytes.drop(20).toByteArray()
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