@file:OptIn(ExperimentalCli::class)

package commands

import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import java.io.File

object Init : Subcommand(
    name = "init",
    actionDescription = "Initializes a git repository in the current folder"
) {
    override fun execute() {
        val gitDir = File(".git")
        gitDir.mkdir()
        File(gitDir, "objects").mkdir()
        File(gitDir, "refs").mkdir()
        File(gitDir, "HEAD").writeText("ref: refs/heads/master\n")

        println("Initialized git directory")
    }
}