@file:OptIn(ExperimentalStdlibApi::class)

package util.model

import java.security.MessageDigest

@JvmInline
value class Hash(private val hash: String) {
    init {
        require(hash.length == 40) { "[Hash.Constructor] Hash string length should be 40 characters long:\n[$hash] (${hash.length})" }
    }

    fun take(n: Int) = hash.take(n)
    fun drop(n: Int) = hash.drop(n)

    companion object {
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

    fun toBytes(): ByteArray {
        return hash.chunked(2).map { it.hexToByte() }.toByteArray()
    }

    override fun toString(): String = hash
}