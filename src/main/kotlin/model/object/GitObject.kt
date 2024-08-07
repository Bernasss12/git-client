package model.`object`

import util.ByteArrayBuilder
import util.model.Hash
import util.model.asString
import util.model.takeLastUntil
import util.model.takeUntil
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.IllegalStateException
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream

sealed class GitObject {
    companion object {
        private val ROOT: Path = Paths.get(".git", "objects")
        private fun getPath(hash: Hash): Path {
            return ROOT.resolve(Path.of(hash.take(2), hash.drop(2)))
        }

        fun splitHeaderAndContent(byteArray: ByteArray): Pair<ByteArray, ByteArray> {
            return byteArray.takeUntil { it == 0.toByte() } to byteArray.takeLastUntil { it == 0.toByte() }
        }

        fun readFromFile(hash: Hash): GitObject? {
            val file = getPath(hash).toFile()
            if (!file.exists()) return null
            InflaterInputStream(FileInputStream(file)).use {
                val bytes = it.readAllBytes()
                val (headerBytes, contentBytes) = splitHeaderAndContent(bytes)
                val (type, length) = headerBytes.asString().split(" ", limit = 2)
                check(contentBytes.size == length.toInt()) { "Content doesn't have the size defined in header: ${contentBytes.size} != $length" }
                return when(type) {
                    Blob.TYPE -> Blob(contentBytes)
                    else -> throw IllegalStateException("[$type] is not an implemented git object type.")
                }
            }
        }
    }

    fun writeToFile() {
        val file = getPath().toFile()
        if (file.exists()) return
        if (!file.mkdirs() && !file.parentFile.exists()) {
            System.err.println("Failed to create parent file: ${file.parentFile}")
            return
        }

        DeflaterOutputStream(FileOutputStream(file)).use { stream ->
            stream.write(getHeaderAndContent())
        }
    }

    fun getHeader(): ByteArray = ByteArrayBuilder.build {
        appendString(getType())
        appendSpace()
        appendString(getLength())
        appendNullChar()
    }

    abstract fun getContent(): ByteArray
    fun getHeaderAndContent(): ByteArray = getHeader() + getContent()

    abstract fun getType(): String
    abstract fun getLength(): Int

    abstract fun getPrintableString(): String

    fun getPath() = getPath(getHash())
    fun getHash() = Hash.fromContentBytes(getHeaderAndContent())
}