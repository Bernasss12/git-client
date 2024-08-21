@file:OptIn(ExperimentalCli::class)

package commands

import Local
import Remote
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.coroutines.runBlocking
import model.DeltaObjectHolder
import model.GitObjectHolder
import model.git.Commit
import model.git.Tree
import model.references.Hash
import kotlin.math.roundToInt

object Clone : Subcommand("clone", "Clone remote repository") {
    override fun execute() {
        /*
            1) Send GET /<repository-name>/info/refs?service=git-upload-pack
            2) Parse response
            3) Send POST /<repository-name>/git-upload-pack
                Headers: - Content-Type: application/x-git-upload-pack-request
                         - Accept: application/x-git-upload-pack-result
                Body:
                     0000want <object-id> <capabilities>
                     00000009done
            4) Parse response (pack-file)
            5) Initialize local git repository
            6) Populate local git repository with pack-file information.
                6a) Write all objects to .git/objects
                6b) Write all references according to initial GET request result.
            7) Write all files defined by the head-tree

        */

        val bytes = runBlocking {
            val remote = "https://github.com/Bernasss12/BetterEnchantedBooks"
            val references = Remote.fetchReferences(remote)
            val objects = Remote.fetchPackFile(remote, references)

            val headRef: Hash = references.find { it.name == "HEAD" }!!.hash
            val ref: String = headRef.let { headHash ->
                references.find { it.hash == headHash }?.name ?: headHash.hash
            }

            Local.writeGitDirectory(ref)

            objects.forEachIndexed { index, holder ->
                val barWidth = 45
                print(
                    "\r[${"#".repeat(((index.toDouble() / objects.size) * barWidth).roundToInt()).padEnd(barWidth)}][${
                        (index + 1).toString().padStart(objects.size.toString().length)
                    }/${objects.size}] Writing git objects..."
                )
                when (holder) {
                    is DeltaObjectHolder -> Unit // TODO implement later
                    is GitObjectHolder -> Local.writeObjectHolderToDisk(holder)
                }
            }
            println()

            val commit = Local.readTypedObjectFromDisk<Commit>(headRef)
            checkNotNull(commit) { "Could not find commit: $headRef" }
            val tree = Local.readTypedObjectFromDisk<Tree>(commit.tree)
            checkNotNull(tree) { "Could not find tree: ${commit.tree}" }
            Local.writeTreeToDisk(tree)


        }
    }
}

