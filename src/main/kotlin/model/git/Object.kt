package model.git

import api.Printable
import model.references.Hash
import util.buildByteArray

sealed class Object : Printable {
    val hash: Hash by lazy {
        Hash.fromContentBytes(getHeaderAndContent())
    }

    companion object {
        fun fromBytes(type: ObjectType, contentBytes: ByteArray): Object {
            return when (type) {
                ObjectType.BLOB -> Blob(contentBytes)
                ObjectType.TREE -> Tree(contentBytes)
                ObjectType.COMMIT -> Commit.fromBytes(contentBytes)
                else -> {
                    throw IllegalArgumentException("$type is not a type that is supported to be written")
                }
            }
        }
    }

    abstract fun getContent(): ByteArray

    fun getHeaderAndContent(): ByteArray = buildByteArray {
        appendString(getType().name.lowercase())
        appendSpace()
        appendString(getLength())
        appendNull()
        appendByteArray(getContent())
    }

    abstract fun getType(): ObjectType

    abstract fun getLength(): Int
}