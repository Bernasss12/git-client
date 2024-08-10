@file:OptIn(ExperimentalCli::class)

package commands

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import model.GitObject
import util.model.Hash

object CatFile : Subcommand(
    name = "cat-file",
    actionDescription = ""
) {
    val hashString by option(
        type = ArgType.String,
        fullName = "hash",
        shortName = "p",
        description = "Blob file hash"
    ).required()

    override fun execute() {
        val hash = Hash(hashString)
        val obj = GitObject.readFromObjectFile(hash)
        print(obj.getPrintableString())
    }
}