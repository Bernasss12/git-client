package model.git

import util.asString
import java.io.File
import java.io.FileInputStream

class Blob(private val byteArray: ByteArray) : Object() {

    companion object {
        val TYPE = ObjectType.BLOB

        fun fromFile(file: File): Blob {
            FileInputStream(file).use {
                return Blob(it.readAllBytes())
            }
        }
    }

    override fun getContent(): ByteArray {
        return byteArray
    }

    override fun getType(): ObjectType = TYPE
    override fun getLength(): Int = byteArray.size
    override fun getPrintableString(): String = byteArray.asString()

}