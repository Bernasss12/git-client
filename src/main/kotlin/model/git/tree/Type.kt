package model.git.tree

enum class Type(val value: Int, val mode: String, val executable: Boolean) {

    TREE(40000, "040000", false),
    BLOB(100644, "100664", false),
    BLOB_EXECUTABLE(100755, "100755", true),
    BLOB_SYMLINK(120000, "120000", false),
    COMMIT(160000, "160000", false);

    companion object {
        fun fromValue(value: Int): Type {
            return entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("$value: is not a valid Permission value.")
        }
    }
}