import exception.ObjectNotFoundException
import model.DeltaObjectHolder
import model.GitObjectHolder
import model.ObjectHolder
import model.ReferenceLine
import model.git.Blob
import model.git.Object
import model.git.ObjectType
import model.git.Tree
import model.git.tree.Type
import model.references.Hash
import model.references.PartialHash
import model.references.Reference
import util.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries

object Local {
    private val FOLDER: File = File("").normalize()
    private val GIT_FOLDER: File = FOLDER + ".git"
    private val GIT_OBJECTS_FOLDER: File = GIT_FOLDER + "objects"
    private val GIT_REFS_FOLDER: File = GIT_FOLDER + "refs"
    private val GIT_HEAD_FILE: File = GIT_FOLDER + "HEAD"

    private const val ROOT_TREE_NAME = ""

    private val gitObjectCache: MutableMap<Hash, Object> = mutableMapOf()

    fun writeGitDirectory(headReference: String) {
        check(GIT_FOLDER.createDirectoryAndParents())
        check(GIT_OBJECTS_FOLDER.createParentDirectories())
        check(GIT_REFS_FOLDER.createParentDirectories())
        GIT_HEAD_FILE.writeText("ref: $headReference\n")
        check(GIT_HEAD_FILE.exists())
    }

    fun writeTreeToDisk(folder: Path, baseTree: Tree) {
        writeTreeToDisk(folder, baseTree, ROOT_TREE_NAME)
    }

    private fun writeTreeToDisk(parent: Path, tree: Tree, name: String) {
        if (parent.createDirectoryAndParents()) {
            tree.entries.forEach { treeEntry ->
                when (val gitObject = treeEntry.gitObject) {
                    is Tree -> writeTreeToDisk(parent = parent + name, tree = gitObject, name = treeEntry.path)
                    is Blob -> writeFileToDisk(parent = parent, gitObject = gitObject, name = treeEntry.path, type = treeEntry.type)
                    else -> throw IllegalStateException("There shouldn't be any tree entries that aren't other trees or blobs.")
                }
            }
        }
    }

    private fun writeFileToDisk(parent: Path, gitObject: Blob, name: String, type: Type) {
        if (parent.exists()) {
            val blobFile = (parent + name).toFile()
            blobFile.writeBytes(gitObject.getContent())
            blobFile.setExecutable(type.executable)
        }
    }

    fun writeReferencesToDisk(references: List<ReferenceLine>) {
        val total = references.size
        references.forEachIndexed { index, reference ->
            printProgressbar(index, total, 100, "Writing references...")
            (GIT_REFS_FOLDER + reference.name).writeText(reference.hash.hash)
        }
    }

    fun writeObjectsToDisk(holders: List<ObjectHolder>) {
        holders.forEachIndexed { index, holder ->
            printProgressbar(index, holders.size, 100, "Writing objects...")
            when (holder) {
                is DeltaObjectHolder -> Unit // TODO implement later
                is GitObjectHolder -> writeObjectHolderToDisk(holder)
            }
        }
    }

    private fun writeObjectHolderToDisk(gitObjectHolder: GitObjectHolder) {
        writeObjectToDisk(Object.fromBytes(gitObjectHolder.type, gitObjectHolder.bytes))
    }

    fun writeObjectToDisk(gitObject: Object) {
        val file = GIT_OBJECTS_FOLDER + gitObject.hash.toFile()

        gitObjectCache.putIfAbsent(gitObject.hash, gitObject)
        if (file.exists()) return
        if (!file.createParentDirectories()) {
            System.err.println("Failed to create parent file: ${file.parentFile}")
            return
        }

        DeflaterOutputStream(FileOutputStream(file)).use { stream ->
            stream.write(gitObject.getHeaderAndContent())
        }
    }

    inline fun <reified T : Object> readTypedObjectFromDiskByReference(hash: Reference): T? {
        return readObjectFromDiskByReference(hash) as? T
    }

    fun readObjectFromDiskByReference(reference: Reference): Object {
        val hash: Hash = when (reference) {
            is Hash -> reference
            is PartialHash -> findHashFromPartialHash(reference)
        }

        return gitObjectCache.computeIfAbsent(hash) {
            readObjectFromDiskByFile(GIT_OBJECTS_FOLDER + hash.toFile())
        }
    }

    inline fun <reified T : Object> readTypedObjectFromDiskByFile(file: File): T? {
        return readObjectFromDiskByFile(file) as? T
    }

    fun readObjectFromDiskByFile(file: File): Object {
        fun splitHeaderAndContent(byteArray: ByteArray): Pair<ByteArray, ByteArray> {
            return byteArray.splitInTwo(NULL_BYTE)
        }

        if (!file.exists()) throw NoSuchFileException(file)

        InflaterInputStream(FileInputStream(file)).use {
            val bytes = it.readAllBytes()
            val (headerBytes, contentBytes) = splitHeaderAndContent(bytes)
            val (type, length) = headerBytes.asString().split(" ", limit = 2)
            check(contentBytes.size == length.toInt()) { "Content doesn't have the size defined in header: ${contentBytes.size} != $length" }
            return Object.fromBytes(ObjectType.valueOf(type.uppercase()), contentBytes)
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
    private operator fun File.plus(other: File): File = resolve(other)

    private operator fun File.plus(other: Path): Path = toPath().resolve(other)
    private operator fun File.plus(other: String): File = resolve(other)
    private fun File.listDirectoryEntries(glob: String): List<Path> = toPath().listDirectoryEntries(glob)
    private fun File.createParentDirectories(): Boolean = parentFile.mkdirs() || parentFile.exists()

    private fun File.createDirectoryAndParents(): Boolean = mkdirs().then { exists() }
    private fun File.notExists(): Boolean = !exists()
    private operator fun Path.plus(other: Path): Path = resolve(other)

    private operator fun Path.plus(other: File): Path = resolve(other.toPath())
    private operator fun Path.plus(other: String): Path = resolve(other)
    private fun Path.createDirectoryAndParents(): Boolean = createDirectories().exists()
}