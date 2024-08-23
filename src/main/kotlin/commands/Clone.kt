@file:OptIn(ExperimentalCli::class)

package commands

import Local
import Remote
import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.optional
import kotlinx.coroutines.runBlocking
import model.git.Commit
import model.git.Tree
import model.references.Hash
import java.nio.file.Path

object Clone : Subcommand("clone", "Clone remote repository") {

    private val remote by argument(
        type = ArgType.String,
        fullName = "remote-url",
        description = "Remote repository that will be clone"
    )
    private val directory by argument(
        type = ArgType.String,
        fullName = "directory",
        description = "Directory where the new repository will be cloned into"
    ).optional()

    override fun execute() {
        val references = runBlocking { Remote.fetchReferences(remote) }
        val holders = runBlocking { Remote.fetchPackFile(remote, references) }

        // Find hash value of HEAD in references
        val headRef: Hash = references.find { it.name == "HEAD" }!!.hash
        // Find reference path for current HEAD
        val ref: String = headRef.let { headHash ->
            references.find { it.hash == headHash }?.name ?: headHash.hash
        }

        // Initializes the local git repository with the defined head ref
        Local.writeGitDirectory(ref)

        // Write all references inside git directory
        Local.writeReferencesToDisk(references)

        // Write all received objects to the object storage in .git/objects
        Local.writeObjectsToDisk(holders)

        // Find HEAD commit (HEAD ref)
        val commit = Local.readTypedObjectFromDiskByReference<Commit>(headRef)
        checkNotNull(commit) { "Could not find commit: $headRef" }

        // Find tree for HEAD commit
        val tree = Local.readTypedObjectFromDiskByReference<Tree>(commit.tree)
        checkNotNull(tree) { "Could not find tree: ${commit.tree}" }

        // Write the current tree files
        Local.writeTreeToDisk(Path.of(directory ?: "."), tree)
    }
}

