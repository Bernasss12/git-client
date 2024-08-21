package model.git

import api.Printable
import util.*
import model.references.Hash
import java.io.FileInputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.InflaterInputStream

sealed class Object : Printable {
    val hash: Hash by lazy {
        Hash.fromContentBytes(getHeaderAndContent())
    }


    abstract fun getContent(): ByteArray
    fun getHeaderAndContent(): ByteArray = buildByteArray {
        appendString(getType())
        appendSpace()
        appendString(getLength())
        appendNull()
        appendByteArray(getContent())
    }

    abstract fun getType(): String
    abstract fun getLength(): Int
}