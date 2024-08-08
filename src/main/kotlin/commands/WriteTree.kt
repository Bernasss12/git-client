@file:OptIn(ExperimentalCli::class)

package commands

import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import model.Tree
import java.nio.file.Paths

object WriteTree : Subcommand("write-tree", "Writes current directory's files to folder.") {
    override fun execute() {
        val currentDirectory = Paths.get(".").toFile()
        val tree = Tree.fromDirectory(currentDirectory, true)
        print(tree.getHash())
    }
}