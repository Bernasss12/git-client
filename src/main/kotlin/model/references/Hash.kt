@file:OptIn(ExperimentalStdlibApi::class)

package model.references

import kotlinx.cli.ArgType
import java.nio.file.Path
import java.security.MessageDigest
import kotlin.io.path.name

class Hash(hash: String) : Reference(hash) {
    init {
        require(hash.length == 40) { "[Hash.Constructor] Hash string length should be 40 characters long:\n[$hash] (${hash.length})" }
    }

    companion object {
        fun fromPath(path: Path): Hash {
            return Hash("${path.parent.name}${path.name}")
        }

        fun fromByteArray(hash: ByteArray): Hash {
            require(hash.size == 20) { "Hash ByteArray should be 20 bytes long:\n[${hash.joinToString { it.toString() }}] (${hash.size})" }
            val stringHash = hash.toHexString()
            return Hash(stringHash)
        }

        fun fromContentBytes(bytes: ByteArray): Hash {
            val digest = MessageDigest.getInstance("SHA-1")
            val hash = digest.digest(bytes)
            return fromByteArray(hash)
        }
    }

    object HashType : ArgType<Hash>(true) {
        override val description: kotlin.String
            get() = "20byte hash value"

        override fun convert(value: kotlin.String, name: kotlin.String): Hash {
            return Hash(value)
        }
    }

    fun toBytes(): ByteArray {
        return hash.chunked(2).map { it.hexToByte() }.toByteArray()
    }

    override fun toString(): String = hash
}