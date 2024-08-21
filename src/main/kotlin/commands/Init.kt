@file:OptIn(ExperimentalCli::class)

package commands

import Local
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand

object Init : Subcommand(
    name = "init",
    actionDescription = "Initializes a git repository in the current folder"
) {
    override fun execute() {
        Local.writeGitDirectory("refs/heads/master")
        println("Initialized git directory")
    }
}