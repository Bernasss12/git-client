package util

class ByteArrayBuilder {
    private var array = byteArrayOf()

    fun appendByte(byte: Byte) {
        array += byte
    }

    fun appendByteArray(bytes: ByteArray) {
        array += bytes
    }

    fun appendChar(char: Char) {
        appendByte(char.code.toByte())
    }

    fun appendString(string: String) {
        appendByteArray(string.toByteArray())
    }

    fun appendString(int: Int) {
        appendString(int.toString())
    }

    fun appendSpace() {
        appendChar(' ')
    }

    fun appendNull() {
        appendByte(NULL_BYTE)
    }

    fun build(): ByteArray = array
}