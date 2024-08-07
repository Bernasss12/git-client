package util.model

import java.security.MessageDigest

@JvmInline
value class Hash(private val hash: String) {
    init {
        require(hash.length == 40)
    }

    fun take(n: Int) = hash.take(n)
    fun drop(n: Int) = hash.drop(n)

    companion object {
        fun fromByteArray(hash: ByteArray): Hash {
            require(hash.size == 20)
            return Hash(hash.joinToString { "%02x".format(it) })
        }

        fun fromContentBytes(bytes: ByteArray): Hash {
            val digest = MessageDigest.getInstance("SHA-1")
            val hash = digest.digest(bytes)
            return fromByteArray(hash)
        }
    }
}