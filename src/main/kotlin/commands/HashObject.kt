@file:OptIn(ExperimentalCli::class)

package commands

import Local
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import model.git.Blob
import java.io.FileInputStream
import java.nio.file.Path

object HashObject : Subcommand(
    name = "hash-object",
    actionDescription = "Compute object ID and optionally create an object from a file"
) {
    private val write by option(
        type = ArgType.Boolean,
        shortName = "w",
        description = "Actually write the object into the object database."
    ).default(false)
    private val fileString by argument(
        type = ArgType.String,
        fullName = "file",
        description = "Hash object as if it were located at the given path."
    )

    override fun execute() {
        val file = Path.of(fileString).toFile()
        if (!file.exists()) {
            System.err.println("File [$fileString] does not exist.")
        }
        val blob = FileInputStream(file).use {
            Blob(it.readAllBytes())
        }
        if (write) {
            Local.writeObjectToDisk(blob)
        }
        println(blob.hash)
    }
}