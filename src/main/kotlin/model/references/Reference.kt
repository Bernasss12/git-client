package model.references

sealed class Reference constructor(val hash: String) {

    companion object {
        fun of(string: String): Reference {
            return if (string.length == 40) {
                Hash(string)
            } else {
                PartialHash(string)
            }
        }
    }

    fun take(n: Int) = hash.take(n)
    fun drop(n: Int) = hash.drop(n)

    fun matches(other: Reference): Boolean {
        return when {
            hash.length < other.hash.length -> {
                other.hash.startsWith(hash)
            }

            hash.length > other.hash.length -> {
                hash.startsWith(other.hash)
            }

            else -> {
                hash == other.hash
            }
        }
    }
}