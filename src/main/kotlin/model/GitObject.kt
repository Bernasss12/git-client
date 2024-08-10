package model

import api.Printable
import util.*
import util.model.Hash
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

sealed class GitObject : Printable {
    companion object {
        private val cache: MutableMap<Hash, GitObject> = mutableMapOf()
        private val ROOT: Path = Paths.get(".git", "objects")
        private fun getPath(hash: Hash): Path {
            return ROOT.resolve(Path.of(hash.take(2), hash.drop(2)))
        }

        private fun splitHeaderAndContent(byteArray: ByteArray): Pair<ByteArray, ByteArray> {
            return byteArray.splitInTwo(NULL_BYTE)
        }

        inline fun <reified T: GitObject> readFromObjectFile(hash: Hash): T {
            return (readFromObjectFile(hash) as? T) ?: throw TypeCastException("$hash is not of type ${T::class.simpleName}")
        }

        fun readFromObjectFile(hash: Hash): GitObject {
            return cache.computeIfAbsent(hash) {
                val file = getPath(hash).toFile()
                if (!file.exists()) throw NoSuchFileException(file)
                InflaterInputStream(FileInputStream(file)).use {
                    val bytes = it.readAllBytes()
                    val (headerBytes, contentBytes) = splitHeaderAndContent(bytes)
                    val (type, length) = headerBytes.asString().split(" ", limit = 2)
                    check(contentBytes.size == length.toInt()) { "Content doesn't have the size defined in header: ${contentBytes.size} != $length" }
                    return@computeIfAbsent when(type) {
                        Blob.TYPE -> Blob(contentBytes)
                        Tree.TYPE -> Tree(contentBytes)
                        else -> throw IllegalStateException("[$type] is not an implemented git object type.")
                    }
                }
            }
        }
    }

    fun writeToFile() {
        val file = getPath().toFile()
        if (file.exists()) return
        if (!file.parentFile.mkdirs() && !file.parentFile.exists()) {
            System.err.println("Failed to create parent file: ${file.parentFile}")
            return
        }

        DeflaterOutputStream(FileOutputStream(file)).use { stream ->
            stream.write(getHeaderAndContent())
        }
    }

    fun getHeader(): ByteArray = buildByteArray {
        appendString(getType())
        appendSpace()
        appendString(getLength())
        appendNull()
    }

    abstract fun getContent(): ByteArray
    fun getHeaderAndContent(): ByteArray = getHeader() + getContent()

    abstract fun getType(): String
    abstract fun getLength(): Int

    fun getPath() = getPath(getHash())
    fun getHash() = Hash.fromContentBytes(getHeaderAndContent())
}