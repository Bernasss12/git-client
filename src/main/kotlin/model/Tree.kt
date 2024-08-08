@file:OptIn(ExperimentalStdlibApi::class)

package model

import api.Printable
import model.Tree.TreeEntry.Companion.hasNextTreeEntry
import util.*
import util.model.Hash
import java.io.File

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

        fun fromDirectory(folder: File, write: Boolean): Tree {
            check(folder.isDirectory) { "$folder is not a directory." }
            return Tree(
                folder.listFiles()?.map { file ->
                    val gitObject = if (file.isDirectory) {
                        fromDirectory(file, write)
                    } else {
                        Blob.fromFile(file)
                    }

                    return@map TreeEntry(
                        permission = when (gitObject) {
                            is Blob -> if (file.canExecute()) Permission.BLOB_EXECUTABLE else Permission.BLOB
                            is Tree -> Permission.TREE
                        },
                        path = file.name,
                        hash = gitObject.getHash()
                    )
                } ?: emptyList()
            )
        }
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
        compareBy<TreeEntry> { it.path }
            .thenBy { it.gitObject?.getHash()?.toString() ?: "" }
    )

    data class TreeEntry(val permission: Permission, val path: String, val hash: Hash) : Printable {

        val gitObject: GitObject? by lazy {
            readFromObjectFile(hash)
        }

        companion object {
            fun consumeBytes(consumer: ByteArrayConsumer): TreeEntry {
                val (metaBytes, hashBytes) = consumer.consumeUntilAfter(20, NULL_BYTE).splitInTwo(NULL_BYTE)
                val hash = Hash.fromByteArray(hashBytes)
                val (mode, path) = metaBytes.asString().split(" ", limit = 2)
                return TreeEntry(Permission.fromValue(mode.toInt()), path, hash)
            }

            fun ByteArrayConsumer.hasNextTreeEntry(): Boolean {
                return hasUntilAfter(20, NULL_BYTE)
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
        BLOB(100644, "100664"),
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