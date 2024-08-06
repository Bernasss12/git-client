package commands

import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import java.io.File

@OptIn(ExperimentalCli::class)
object Init: Subcommand("init", "Initializes a git repository in the current folder") {
    override fun execute() {
        val gitDir = File(".git")
        gitDir.mkdir()
        File(gitDir, "objects").mkdir()
        File(gitDir, "refs").mkdir()
        File(gitDir, "HEAD").writeText("ref: refs/heads/master\n")

        println("Initialized git directory")
    }
}