@file:OptIn(ExperimentalCli::class)

package commands

import Local
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import model.references.Reference

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
        val hash = Reference.of(hashString)
        val obj = Local.readObjectFromDisk(hash)
        print(obj.getPrintableString())
    }
}