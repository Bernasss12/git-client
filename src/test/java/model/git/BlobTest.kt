@file:OptIn(ExperimentalStdlibApi::class)

package model.git

import Local
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import util.printByteArrayComparison
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.zip.InflaterInputStream

class BlobTest {

    private lateinit var exampleBlobFile: File
    private lateinit var exampleBlobBytes: ByteArray

    private val exampleBlobCat =
        """|package model
           |
           |import model.git.ObjectType
           |import model.references.Hash
           |
           |sealed class ObjectHolder
           |
           |class GitObjectHolder(val type: ObjectType, val bytes: ByteArray) : ObjectHolder()
           |class DeltaObjectHolder(val type: ObjectType, val hash: Hash, val bytes: ByteArray) : ObjectHolder()""".trimMargin().trimIndent()

    @BeforeEach
    fun setUp() {
        exampleBlobFile = Paths.get(javaClass.getResource("/blob/example2")?.toURI() ?: error("[File] This needs to happen.")).toFile()
        exampleBlobBytes = InflaterInputStream(FileInputStream(exampleBlobFile)).use {
            it.readAllBytes()
        }
    }

    @Test
    fun testGitBlobFromFile() {
        val blob = Local.readTypedObjectFromDiskByFile<Blob>(exampleBlobFile) ?: error("[Read] This needs to happen.")

        printByteArrayComparison(exampleBlobBytes, blob.getHeaderAndContent())

        assertArrayEquals(blob.getHeaderAndContent(), exampleBlobBytes)
    }

    @Test
    fun testGitBlobPrintableString() {
        val blob = Local.readTypedObjectFromDiskByFile<Blob>(exampleBlobFile) ?: error("[Read] This needs to happen.")

        printByteArrayComparison(exampleBlobCat.toByteArray(), blob.getPrintableString().toByteArray())

        assertEquals(exampleBlobCat, blob.getPrintableString())
    }
}