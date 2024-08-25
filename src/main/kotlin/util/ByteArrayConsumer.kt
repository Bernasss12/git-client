package util

import java.util.zip.Inflater

class ByteArrayConsumer(private var bytes: ByteArray) {

    companion object {
        fun <T> ByteArray.consume(block: ByteArrayConsumer.() -> T): T {
            val consumer = ByteArrayConsumer(this)
            return consumer.block()
        }
    }

    fun consumeUntil(byte: Byte): ByteArray {
        val index = bytes.indexOf(byte)
        val result = consume(index)
        erase(index + 1)
        return result
    }

    fun consume(count: Int, skip: Int): ByteArray {
        erase(skip)
        return consume(count - skip)
    }

    fun consume(count: Int): ByteArray {
        val result = get(count)
        erase(count)
        return result
    }

    fun consumeLast(count: Int): ByteArray {
        val result = getLast(count)
        eraseLast(count)
        return result
    }

    fun consume(): Byte {
        val result = get(1)
        erase(1)
        return result[0]
    }

    fun peekAll() = bytes.copyOf()
    fun peek(count: Int = 1) = get(count)

    private fun get(count: Int) = bytes.takeAsByteArray(count)
    private fun getLast(count: Int) = bytes.takeLastAsByteArray(count)
    private fun erase(count: Int) {
        bytes = bytes.dropAsByteArray(count)
    }

    private fun eraseLast(count: Int) {
        bytes = bytes.dropLastAsByteArray(count)
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

    fun hasNext(): Boolean {
        return bytes.isNotEmpty()
    }

    fun consumeNextCompressed(): ByteArray {
        val buffer = ByteArray(1024 * 1024)
        val inf = Inflater()
        inf.setInput(bytes)
        inf.inflate(buffer)
        erase(inf.totalIn)
        return buffer.takeAsByteArray(inf.totalOut)
    }
}