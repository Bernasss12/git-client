@file:OptIn(ExperimentalCli::class)

package commands

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import model.Blob
import java.io.FileInputStream
import java.nio.file.Path

object HashObject : Subcommand("hash-object", "Compute object ID and optionally create an object from a file") {
    private val write by option(ArgType.Boolean, shortName = "w", description = "Actually write the object into the object database.").default(false)
    private val fileString by argument(ArgType.String, "file", "Hash object as if it were located at the given path.")

    override fun execute() {
        val file = Path.of(fileString).toFile()
        if (!file.exists()) {
            System.err.println("File [$fileString] does not exist.")
        }
        val blob = FileInputStream(file).use {
            Blob(it.readAllBytes())
        }
        if (write) {
            blob.writeToFile()
        }
    }
}