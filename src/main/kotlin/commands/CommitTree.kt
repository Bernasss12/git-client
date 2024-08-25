@file:OptIn(ExperimentalCli::class)

package commands

import Local
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import model.git.Commit
import model.references.Hash

object CommitTree : Subcommand(
    name = "commit-tree",
    actionDescription = "Create a new commit object"
) {
    private val tree by argument(
        type = Hash.HashType,
        description = "Existing tree object hash"
    )
    private val parent by option(
        type = Hash.HashType,
        shortName = "p",
        description = "Parent commit hash"
    )
    private val message by option(
        type = ArgType.String,
        shortName = "m",
        description = "Commit description"
    ).required()

    override fun execute() {
        val commit = Commit.new(
            tree,
            parent,
            "$message\n"
        )
        Local.writeObjectToDisk(commit)

        println(commit.hash)
    }
}