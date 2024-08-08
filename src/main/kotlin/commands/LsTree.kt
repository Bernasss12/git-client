@file:OptIn(ExperimentalCli::class)

package commands

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.default
import model.GitObject
import model.Tree
import util.model.Hash

object LsTree : Subcommand("ls-tree", "List the contents of a tree object") {
    private val nameOnly by option(ArgType.Boolean, fullName = "name-only", description = "List only filenames.").default(false)
    private val hash by argument(ArgType.String, "file", "Hash object as if it were located at the given path.")

    override fun execute() {
        val gitObject = requireNotNull(GitObject.readFromFile(Hash(hash))) { "Tree object not found: $hash" }
        val tree = requireNotNull(gitObject as? Tree) { "Object was not a tree." }
        System.err.println("Name only: $nameOnly; Hash: $hash")
        print(
            if (nameOnly) {
                tree.getPrintableStringNameOnly()
            } else {
                tree.getPrintableString()
            }
        )
    }
}