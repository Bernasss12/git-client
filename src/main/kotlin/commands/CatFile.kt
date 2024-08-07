@file:OptIn(ExperimentalCli::class)

package commands

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import model.GitObject
import util.model.Hash

object CatFile : Subcommand("cat-file", "") {
    val hashString by option(ArgType.String, "hash", "p", "Blob file hash").required()

    override fun execute() {
        val hash = Hash(hashString)
        val obj = GitObject.readFromFile(hash) ?: return
        print(obj.getPrintableString())
    }
}