package model

import api.Printable
import model.Tree.TreeEntry.Companion.hasNextTreeEntry
import util.ByteArrayConsumer
import util.NULL_BYTE
import util.asString
import util.buildByteArray
import util.model.Hash

class Tree private constructor(val entries: List<TreeEntry>, val bytes: ByteArray) : GitObject() {

    constructor(entries: List<TreeEntry>) : this(
        entries = entries,
        bytes = buildByteArray {
            entries.forEach { entry -> appendByteArray(entry.toBytes()) }
        }
    )

    constructor(bytes: ByteArray) : this(
        entries = buildList {
            val consumer = ByteArrayConsumer(bytes)
            while (consumer.hasNextTreeEntry()) {
                add(TreeEntry.consumeBytes(consumer))
            }
        },
        bytes = bytes
    )

    companion object {
        const val TYPE = "tree"
    }

    override fun getContent(): ByteArray = bytes

    override fun getType(): String = TYPE

    override fun getLength(): Int = getContent().size

    override fun getPrintableString(): String = buildString {
        getSortedChildren().forEach {
            append(it.getPrintableString())
            append('\n')
        }
    }

    fun getPrintableStringNameOnly(): String = buildString {
        getSortedChildren().forEach {
            append(it.path)
            append('\n')
        }
    }

    private fun getSortedChildren(): List<TreeEntry> = entries.sortedWith(
        compareBy<TreeEntry> { it.permission.mode }
            .thenBy { it.path }
            .thenBy { it.gitObject?.getHash()?.toString() ?: "" }
    )

    data class TreeEntry(val permission: Permission, val path: String, val hash: Hash) : Printable {

        val gitObject: GitObject? by lazy {
            readFromFile(hash)
        }

        companion object {
            fun consumeBytes(consumer: ByteArrayConsumer): TreeEntry {
                val (mode, path) = consumer.consumeUntil(NULL_BYTE).asString().split(' ', limit = 2)
                val hash = Hash(consumer.consume(40).asString())
                return TreeEntry(Permission.fromValue(mode.toInt()), path, hash)
            }

            fun ByteArrayConsumer.hasNextTreeEntry(): Boolean {
                return hasAfter(40, NULL_BYTE)
            }
        }

        override fun getPrintableString(): String {
            return buildString {
                append(permission.mode)
                append(" ")
                append(gitObject?.getType() ?: "")
                append(hash)
                append("\t")
                append(path)
            }
        }

        fun toBytes(): ByteArray = buildByteArray {
            appendString(permission.value)
            appendSpace()
            appendString(path)
            appendNull()
            appendByteArray(hash.toBytes())
        }
    }

    enum class Permission(val value: Int, val mode: String) {
        TREE(40000, "040000"),
        BLOB(100664, "100664"),
        BLOB_EXECUTABLE(100755, "100755"),
        BLOB_SYMLINK(120000, "120000"),
        COMMIT(160000, "160000");

        companion object {
            fun fromValue(value: Int): Permission {
                return entries.firstOrNull { it.value == value } ?: throw IllegalArgumentException("$value: is not a valid Permission value.")
            }
        }
    }
}