package util

class ByteArrayBuilder {
    companion object {
        fun build(block: ByteArrayBuilder.() -> Unit) : ByteArray {
            val bytes = ByteArrayBuilder()
            block.invoke(bytes)
            return bytes.build()
        }
    }

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

    fun appendNullChar() {
        appendChar('\u0000')
    }

    fun build(): ByteArray = array
}