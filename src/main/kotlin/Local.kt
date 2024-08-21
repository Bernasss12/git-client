import exception.ObjectNotFoundException
import model.git.Blob
import model.git.Commit
import model.git.Object
import model.git.Tree
import model.references.Hash
import model.references.PartialHash
import model.references.Reference
import util.NULL_BYTE
import util.asString
import util.splitInTwo
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream
import kotlin.io.path.listDirectoryEntries

object Local {
    val GIT_FOLDER: File = File(".git")
    val GIT_OBJECTS_FOLDER: File = GIT_FOLDER + "objects"
    val GIT_REFS_FOLDER: File = GIT_FOLDER + "refs"
    val GIT_HEAD_FILE: File = GIT_FOLDER + "HEAD"

    private val gitObjectCache: MutableMap<Hash, Object> = mutableMapOf()

    fun writeGitDirectory(headReference: String) {
        check(GIT_FOLDER.createDirectoryAndParents())
        check(GIT_OBJECTS_FOLDER.createParentDirectories())
        check(GIT_REFS_FOLDER.createParentDirectories())
        GIT_HEAD_FILE.writeText("ref: $headReference\n")
        check(GIT_HEAD_FILE.exists())
    }

    fun writeObjectToDisk(gitObject: Object) {
        val file = GIT_OBJECTS_FOLDER + gitObject.hash.hash
        if (file.exists()) return
        if (!file.createParentDirectories()) {
            System.err.println("Failed to create parent file: ${file.parentFile}")
            return
        }

        DeflaterOutputStream(FileOutputStream(file)).use { stream ->
            stream.write(gitObject.getHeaderAndContent())
        }
    }

    inline fun <reified T : Object> readTypedObjectFromDisk(hash: Reference): T? {
        return readObjectFromDisk(hash) as? T
    }

    fun readObjectFromDisk(reference: Reference): Object {
        val hash: Hash = when (reference) {
            is Hash -> reference
            is PartialHash -> findHashFromPartialHash(reference)
        }

        return readObjectFromDisk(hash)
    }

    private fun readObjectFromDisk(hash: Hash): Object {
        fun splitHeaderAndContent(byteArray: ByteArray): Pair<ByteArray, ByteArray> {
            return byteArray.splitInTwo(NULL_BYTE)
        }

        return gitObjectCache.computeIfAbsent(hash) {
            val file = hash.toFile()
            if (!file.exists()) throw NoSuchFileException(file)
            InflaterInputStream(FileInputStream(file)).use {
                val bytes = it.readAllBytes()
                val (headerBytes, contentBytes) = splitHeaderAndContent(bytes)
                val (type, length) = headerBytes.asString().split(" ", limit = 2)
                check(contentBytes.size == length.toInt()) { "Content doesn't have the size defined in header: ${contentBytes.size} != $length" }
                return@computeIfAbsent when (type) {
                    Blob.TYPE -> Blob(contentBytes)
                    Tree.TYPE -> Tree(contentBytes)
                    Commit.TYPE -> Commit.fromBytes(contentBytes)
                    else -> throw IllegalStateException("[$type] is not an implemented git object type.")
                }
            }
        }
    }

    private fun findHashFromPartialHash(partialHash: PartialHash): Hash {
        val parent = GIT_OBJECTS_FOLDER.resolve(partialHash.take(2))
        if (parent.notExists()) throw ObjectNotFoundException("${parent.absolutePath} does not exist.")
        return parent
            .listDirectoryEntries("${partialHash.drop(2)}*")
            .singleOrNull()
            ?.let { Hash.fromPath(it) }
            ?: throw ObjectNotFoundException("No objects matching partial hash ${partialHash.hash} were found.")
    }

    private fun Reference.toFile(): File = Paths.get(take(2), drop(2)).toFile()

    private operator fun File.plus(other: Path): Path = toPath().resolve(other)
    private operator fun File.plus(other: String): File = resolve(other)

    private fun File.listDirectoryEntries(glob: String): List<Path> = toPath().listDirectoryEntries()
    private fun File.createParentDirectories(): Boolean = parentFile.mkdirs() || parentFile.exists()
    private fun File.createDirectoryAndParents(): Boolean = mkdirs() || exists()
    private fun File.notExists(): Boolean = !exists()
}