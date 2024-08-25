import Remote.consumeVariableLenghtInteger
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
import util.ByteArrayConsumer.Companion.consume
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterInputStream
import kotlin.io.path.*

object Local {
    var directory = "."

    private val FOLDER: Path
        get() = Path.of(directory).normalize()
    private val GIT_FOLDER: Path
        get() = FOLDER + ".git"
    private val GIT_OBJECTS_FOLDER: Path
        get() = GIT_FOLDER + "objects"
    private val GIT_REFS_FOLDER: Path
        get() = GIT_FOLDER + "refs"
    private val GIT_HEAD_FILE: File
        get() = GIT_FOLDER.toFile() + "HEAD"

    private val ROOT_TREE_NAME = ""

    private val gitObjectCache: MutableMap<Hash, Object> = mutableMapOf()

    fun writeGitDirectory(headReference: String) {
        check(GIT_FOLDER.createDirectoryAndParents())
        check(GIT_OBJECTS_FOLDER.createDirectoryAndParents())
        check(GIT_REFS_FOLDER.createDirectoryAndParents())
        GIT_HEAD_FILE.writeText("ref: $headReference\n")
        check(GIT_HEAD_FILE.exists())
    }

    fun writeTreeToDisk(baseTree: Tree) {
        writeTreeToDisk(FOLDER, baseTree, ROOT_TREE_NAME)
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
            val file: File = GIT_FOLDER + File(reference.name)
            if (file.createParentDirectories()) {
                printProgressbar(index, total, 100, "Writing references...")
                file.writeText(reference.hash.hash)
            }
        }
    }

    fun writeObjectsToDisk(holders: List<ObjectHolder>) {
        val (gitObjects, deltas) = holders.partitionByType<GitObjectHolder, DeltaObjectHolder>()
        gitObjects.forEachIndexed { index, holder ->
            printProgressbar(index, gitObjects.size, 100, "Writing objects...")
            writeObjectFromHolderToDisk(holder)
        }
        deltas.forEachIndexed { index, holder ->
            printProgressbar(index, deltas.size, 100, "Applying deltas...")
            writeDeltaObjectHolderToDisk(holder)
        }
    }

    sealed class Instruction {
        companion object {
            private fun isCopy(byte: UByte) = (byte and 0b1000_0000u) != 0b0u.toUByte()
            private fun isInsert(byte: UByte) = !isCopy(byte)

            // When copy
            private fun getCopyOffset(byte: UByte) = (byte and 0b1111u).countOneBits()
            private fun getCopySize(byte: UByte) = (byte and 0b0111_0000u).countOneBits()

            // When insert
            private fun getInsertSize(byte: UByte) = byte.toInt()

            fun getInstruction(byte: UByte): Instruction {
                return if (isCopy(byte)) {
                    CopyInstruction(getCopyOffset(byte), getCopySize(byte))
                } else {
                    InsertInstruction(getInsertSize(byte))
                }
            }
        }
    }

    data class CopyInstruction(val offset: Int, val size: Int) : Instruction()
    data class InsertInstruction(val length: Int) : Instruction()

    private fun writeDeltaObjectHolderToDisk(holder: DeltaObjectHolder) {
        val newObject = holder.bytes.consume {
            val original = readObjectFromDiskByReference(holder.hash).getContent()
            val originalSize = consumeVariableLenghtInteger()
            val resultSize = consumeVariableLenghtInteger()
            check(original.size == originalSize)  {
                "Delta cannot be applied because original file did not match expected size: E:$originalSize != A:${original.size}"
            }
            val result = buildByteArray {
                while (hasNext()) {
                    val byte = consume().toUByte()
                    when (val instruction = Instruction.getInstruction(byte)) {
                        is CopyInstruction -> {
                            val offsetBytes = peek(instruction.offset)
                            val sizeBytes = peek(instruction.size)
                            val offset = consume(instruction.offset).toLEInt()
                            val size = consume(instruction.size).toLEInt()
                            appendByteArray(original.takeRange(offset, size))
                        }

                        is InsertInstruction -> {
                            appendByteArray(consume(instruction.length))
                        }
                    }
                }
            }
            check(result.size == resultSize) {
                "Delta was applied but result did not match expected size: E:$resultSize != A:${result.size}"
            }
            Object.fromBytes(ObjectType.BLOB, result)
        }
        writeObjectToDisk(newObject)
    }

    private fun writeObjectFromHolderToDisk(gitObjectHolder: GitObjectHolder) {
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
        if (parent.notExists()) throw ObjectNotFoundException("${parent.absolutePathString()} does not exist.")
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

    private operator fun Path.plus(other: File): File = resolve(other.toPath()).toFile()
    private operator fun Path.plus(other: String): Path = resolve(other)
    private fun Path.createDirectoryAndParents(): Boolean = createDirectories().exists()
}