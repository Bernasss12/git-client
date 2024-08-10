package model

import util.asString
import util.buildByteArray
import util.model.Hash
import util.model.Identity
import util.model.Parents
import util.model.Timestamp

class Commit private constructor(
    private val tree: Hash,
    private val parents: Parents,
    private val author: Identity,
    private val commiter: Identity,
    private val authoredTimestamp: Timestamp,
    private val commitedTimestamp: Timestamp,
    private val message: String,
) : GitObject() {

    companion object {
        const val TYPE = "commit"

        fun new(
            tree: Hash,
            parent: Hash?,
            message: String,
        ): Commit {
            return Commit(
                tree = tree,
                parents = Parents(
                    primary = parent,
                    secondary = null
                ),
                author = Identity.DEFAULT,
                commiter = Identity.DEFAULT,
                authoredTimestamp = Timestamp.now(),
                commitedTimestamp = Timestamp.now(),
                message = message
            )
        }

        fun fromBytes(bytes: ByteArray): Commit {
            val (meta, message) = bytes.asString().split("\n\n")
            val lines = meta.split("\n").map { line -> line.split(" ", limit = 2).let { it[0] to it[1] } }
            val tree = lines["tree"]?.let { Hash(it) } ?: throw IllegalStateException("Tree is required in commit")
            val parents = Parents(
                lines["parent"]?.let { Hash(it) },
                lines.getLastIfMultiple("parent")?.let { Hash(it) }
            )
            val (author, authoringTime) = lines["author"]?.parseIdentityAndTimestamp() ?: throw IllegalStateException("Author is required in commit")
            val (committer, committingTime) = lines["committer"]?.parseIdentityAndTimestamp() ?: throw IllegalStateException("Committer is required in commit")

            return Commit(
                tree = tree,
                parents = parents,
                author = author,
                commiter = committer,
                authoredTimestamp = authoringTime,
                commitedTimestamp = committingTime,
                message = message,
            )
        }

        private fun String.parseIdentityAndTimestamp(): Pair<Identity, Timestamp> {
            val (identityPortion, timestampPortion) = split("> ", limit = 2)
            val (name, email) = identityPortion.split(" <", limit = 2)
            return Identity(name, email) to Timestamp.fromString(timestampPortion)
        }

        private operator fun List<Pair<String, String>>.get(key: String): String? {
            return find { it.first == key }?.second
        }

        private fun List<Pair<String, String>>.getLastIfMultiple(key: String): String? {
            return takeIf { it.count { it.first == "parent" } > 1 }?.findLast { it.first == "parent" }?.second
        }
    }

    override fun getContent(): ByteArray = buildByteArray {
        appendLine("tree $tree")
        parents.primary?.let { appendLine("parent $it") }
        parents.secondary?.let { appendLine("parent $it") }
        appendLine("author ${author.toIdentityString()} ${authoredTimestamp.toTimestampString()}")
        appendLine("committer ${commiter.toIdentityString()} ${commitedTimestamp.toTimestampString()}")
        appendChar('\n')
        appendString(message)
    }

    override fun getType(): String = TYPE

    override fun getLength(): Int = getContent().size

    override fun getPrintableString(): String {
        TODO("Not yet implemented")
    }
}