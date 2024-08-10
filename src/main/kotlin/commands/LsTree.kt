@file:OptIn(ExperimentalCli::class)

package commands

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import model.GitObject
import model.Tree
import util.model.Hash

object LsTree : Subcommand(
    name = "ls-tree",
    actionDescription = "List the contents of a tree object"
) {
    private val nameOnly by option(
        type = ArgType.Boolean,
        fullName = "name-only",
        description = "List only filenames."
    ).default(false)
    private val hash by argument(
        type = ArgType.String,
        fullName = "file",
        description = "Hash object as if it were located at the given path."
    )

    override fun execute() {
        val tree = requireNotNull(GitObject.readTypedFromObjectFile<Tree>(Hash(hash))) { "Tree object not found: $hash" }
        print(
            if (nameOnly) {
                tree.getPrintableStringNameOnly()
            } else {
                tree.getPrintableString()
            }
        )
    }
}