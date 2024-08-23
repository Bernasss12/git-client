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

class CommitTest {

    private lateinit var exampleCommitFile: File
    private lateinit var exampleCommitBytes: ByteArray

    @BeforeEach
    fun setUp() {
        exampleCommitFile = Paths.get(javaClass.getResource("/commit/example")?.toURI() ?: error("[File] This needs to happen.")).toFile()
        exampleCommitBytes = InflaterInputStream(FileInputStream(exampleCommitFile)).use {
            it.readAllBytes()
        }
    }

    @Test
    fun testGitCommitFromFile() {
        val commit = Local.readTypedObjectFromDiskByFile<Commit>(exampleCommitFile) ?: error("[Read] This needs to happen.")

        printByteArrayComparison(exampleCommitBytes, commit.getHeaderAndContent())

        assertArrayEquals(commit.getHeaderAndContent(), exampleCommitBytes)
    }
}