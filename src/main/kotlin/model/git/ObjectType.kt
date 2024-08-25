package model.git

enum class ObjectType(val value: Int) {
    COMMIT(1),
    TREE(2),
    BLOB(3),
    TAG(4),
    RESERVED(5),
    OFS_DELTA(6),
    REFERENCE_DELTA(7);

    companion object {
        fun fromValue(value: Byte): ObjectType {
            return entries.find { it.value == value.toInt() }
                ?: throw IllegalArgumentException("$value [${value.toInt().toBinaryString()}] is not a valid git object type code")
        }

        fun Int.toBinaryString() = Integer.toBinaryString(this)
    }
}