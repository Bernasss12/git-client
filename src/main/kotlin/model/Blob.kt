package model

import util.asString

class Blob(val byteArray: ByteArray) : GitObject() {

    companion object {
        const val TYPE = "blob"
    }

    override fun getContent(): ByteArray {
        return byteArray
    }

    override fun getType(): String = TYPE
    override fun getLength(): Int = byteArray.size
    override fun getPrintableString(): String = byteArray.asString()

}