package model

import util.asString
import java.io.File
import java.io.FileInputStream

class Blob(val byteArray: ByteArray) : GitObject() {

    companion object {
        const val TYPE = "blob"

        fun fromFile(file: File): Blob {
            FileInputStream(file).use {
                return Blob(it.readAllBytes())
            }
        }
    }

    override fun getContent(): ByteArray {
        return byteArray
    }

    override fun getType(): String = TYPE
    override fun getLength(): Int = byteArray.size
    override fun getPrintableString(): String = byteArray.asString()

}