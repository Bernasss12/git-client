package model.git

import api.Printable
import model.git.Tree.TreeEntry.Companion.hasNextTreeEntry
import model.git.tree.Type
import util.*
import model.references.Hash
import java.io.File

class Tree private constructor(val entries: List<TreeEntry>, private val bytes: ByteArray) : Object() {

    constructor(entries: List<TreeEntry>) : this(
        entries = entries,
        bytes = buildByteArray {
            getSorted(entries).forEach { entry -> appendByteArray(entry.toBytes()) }
        }
    )

    constructor(bytes: ByteArray) : this(
        entries = getSorted(
            buildList {
                val consumer = ByteArrayConsumer(bytes)
                while (consumer.hasNextTreeEntry()) {
                    add(TreeEntry.consumeBytes(consumer))
                }
            }
        ),
        bytes = bytes
    )

    companion object {
        val TYPE = ObjectType.TREE

        fun fromDirectory(folder: File, write: Boolean): Tree {
            check(folder.isDirectory) { "$folder is not a directory." }
            return Tree(
                entries = folder.listFiles()?.filter {
                    it.name != ".git"
                }?.map { file ->
                    val gitObject = if (file.isDirectory) {
                        fromDirectory(file, write)
                    } else {
                        Blob.fromFile(file)
                    }.also { tree ->
                        if (write) Local.writeObjectToDisk(tree)
                    }

                    @Suppress("KotlinConstantConditions")
                    return@map TreeEntry(
                        type = when (gitObject) {
                            is Commit, is Blob -> if (file.canExecute()) Type.BLOB_EXECUTABLE else Type.BLOB
                            is Tree -> Type.TREE
                        },
                        path = file.name,
                        hash = gitObject.hash
                    )
                } ?: emptyList()
            ).also { tree ->
                if (write) Local.writeObjectToDisk(tree)
            }
        }

        fun getSorted(list: List<TreeEntry>) = list.sortedWith(
            compareBy<TreeEntry> { it.path }
                .thenBy { it.gitObject?.hash?.toString() ?: "" }
        )
    }

    override fun getContent(): ByteArray = bytes

    override fun getType(): ObjectType = TYPE

    override fun getLength(): Int = getContent().size

    override fun getPrintableString(): String = buildString {
        getSortedEntries().forEach {
            append(it.getPrintableString())
            append('\n')
        }
    }

    fun getPrintableStringNameOnly(): String = buildString {
        getSortedEntries().forEach {
            append(it.path)
            append('\n')
        }
    }

    private fun getSortedEntries(): List<TreeEntry> = getSorted(entries)

    data class TreeEntry(val type: Type, val path: String, val hash: Hash) : Printable {

        val gitObject: Object? by lazy {
            Local.readObjectFromDiskByReference(hash)
        }

        companion object {
            fun consumeBytes(consumer: ByteArrayConsumer): TreeEntry {
                val (metaBytes, hashBytes) = consumer.consumeUntilAfter(20, NULL_BYTE).splitInTwo(NULL_BYTE)
                val hash = Hash.fromByteArray(hashBytes)
                val (mode, path) = metaBytes.asString().split(" ", limit = 2)
                return TreeEntry(Type.fromValue(mode.toInt()), path, hash)
            }

            fun ByteArrayConsumer.hasNextTreeEntry(): Boolean {
                return hasUntilAfter(20, NULL_BYTE)
            }
        }

        override fun getPrintableString(): String {
            return buildString {
                append(type.mode)
                append(" ")
                append(gitObject?.getType() ?: "")
                append(hash)
                append("\t")
                append(path)
            }
        }

        fun toBytes(): ByteArray = buildByteArray {
            appendString(type.value)
            appendSpace()
            appendString(path)
            appendNull()
            appendByteArray(hash.toBytes())
        }
    }
}