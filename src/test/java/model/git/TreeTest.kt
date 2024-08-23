@file:OptIn(ExperimentalStdlibApi::class)

package model.git

import Local
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import util.printByteArrayComparison
import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.zip.InflaterInputStream

class TreeTest {

    private lateinit var exampleTreeFile: File
    private lateinit var exampleTreeBytes: ByteArray

    @BeforeEach
    fun setUp() {
        exampleTreeFile = Paths.get(javaClass.getResource("/tree/example")?.toURI() ?: error("[File] This needs to happen.")).toFile()
        exampleTreeBytes = InflaterInputStream(FileInputStream(exampleTreeFile)).use {
            it.readAllBytes()
        }
    }

    @Test
    fun testGitCommitFromFile() {
        val tree = Local.readTypedObjectFromDiskByFile<Tree>(exampleTreeFile) ?: error("[Read] This needs to happen.")

        printByteArrayComparison(exampleTreeBytes, tree.getHeaderAndContent())

        assertArrayEquals(tree.getHeaderAndContent(), exampleTreeBytes)
    }
}