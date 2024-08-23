@file:OptIn(ExperimentalStdlibApi::class)

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import model.DeltaObjectHolder
import model.GitObjectHolder
import model.ObjectHolder
import model.git.ObjectType
import model.git.ObjectType.*
import model.references.Hash
import util.*
import java.nio.charset.StandardCharsets
import kotlin.math.roundToInt

object Remote {
    val HTTP_CLIENT = HttpClient(CIO)

    suspend fun fetchReferences(remoteUrl: String): List<ReferenceLine> {
        val response = HTTP_CLIENT.get {
            url(baseUrl = remoteUrl) {
                appendEncodedPathSegments("info", "refs")
                parameters.append(
                    name = "service",
                    value = "git-upload-pack"
                )
            }
        }

        return ByteArrayConsumer(response.readBytes()).consumeRefsPckLines()
    }

    suspend fun fetchPackFile(remoteUrl: String, references: List<ReferenceLine>): List<ObjectHolder> {
        val packRequestBody = references.joinToString(separator = "\n", transform = { "0032want ${it.hash}" }, postfix = "\n00000009done\n")

        val packResponse = HTTP_CLIENT.post {
            url(remoteUrl) {
                appendEncodedPathSegments("git-upload-pack")
            }
            headers {
                append(HttpHeaders.ContentType, "application/x-git-upload-pack-request")
                append(HttpHeaders.Accept, "application/x-git-upload-pack-result")
            }
            setBody(packRequestBody.toByteArray(StandardCharsets.UTF_8))
        }

        val bytes = packResponse.readBytes()
        val consumer = ByteArrayConsumer(bytes)

        val hash = Hash.fromByteArray(consumer.consumeLast(20))
        val computedHash = Hash.fromContentBytes(consumer.peekAll())

        if (hash != computedHash) {
            System.err.println(
                """
                    Computed and received hashes don't match.
                    $hash
                    $computedHash
                    This is very likely an implementation error and will be ignored for now.
                    """.trimIndent()
            )
        }

        val startLength = consumer.peek(4).toInt()
        val start = consumer.consume(startLength, 4).asString()
        check(start == "NAK\n") { "Must be have NAK." }

        val signature = consumer.consume(4).asString()
        check(signature == "PACK") { "Signature must be 'PACK', not $signature" }
        val version = consumer.consume(4).toIntRaw()
        check(version in listOf(2, 3)) { "Version $version not supported." }
        val objectCount = consumer.consume(4).toIntRaw()

        return buildList {
            repeat(objectCount) { index ->

                val (size, type) = consumer.parseSizeAndType()
                val barWidth = 45
                print(
                    "\r[${"#".repeat(((index.toDouble() / objectCount) * barWidth).roundToInt()).padEnd(barWidth)}][${
                        (index + 1).toString().padStart(objectCount.toString().length)
                    }/$objectCount] Parsing downloaded objects..."
                )
                when (type) {
                    COMMIT, TREE, BLOB -> {
                        val objectBytes = consumer.consumeNextCompressed()
                        check(objectBytes.size == size) { "$size does not match array size: ${objectBytes.size}" }
                        add(GitObjectHolder(type, objectBytes))
                    }

                    TAG, RESERVED -> TODO("TAG AND RESERVED WERE NOT IMPLEMENTED YET")
                    REFERENCE_DELTA, OFS_DELTA -> {
                        if (type == OFS_DELTA) TODO("OFFSET DELTA WAS NOT IMPLEMENTED YET")
                        val deltaHash = Hash.fromByteArray(consumer.consume(20))
                        val deltaBytes = consumer.consumeNextCompressed()
                        add(DeltaObjectHolder(type, deltaHash, deltaBytes))
                    }
                }
            }
            println()
        }
    }

    private const val CONTINUATION_MASK = 0b10000000

    private const val TYPE_MASK = 0b01110000
    private const val TYPE_SHIFT = 4

    private const val INITIAL_SIZE_BIT_COUNT = 4
    private const val INITIAL_SIZE_MASK = 0b00001111

    private const val CONTINUATION_SIZE_BIT_COUNT = 7
    private const val CONTINUATION_SIZE_MASK = 0b01111111

    private const val ONE_BYTE_MASK = 0xFF

    private fun ByteArrayConsumer.parseSizeAndType(): Pair<Int, ObjectType> {
        // Consume next byte and clear any possible stray bits
        fun nextByte() = consume().toInt() and ONE_BYTE_MASK

        // Checks the continuation bit
        fun extra(byte: Int): Boolean = (byte and CONTINUATION_MASK) != 0


        val byte = nextByte()

        // Extract the 3 bit value representing the type of the object
        val typeValue = (byte and TYPE_MASK) shr TYPE_SHIFT

        // Extract the first 4 bits of the size number
        var size = byte and INITIAL_SIZE_MASK

        var needsExtra = extra(byte)
        var usedBits = INITIAL_SIZE_BIT_COUNT
        while (needsExtra) {
            val nextByte = nextByte()

            // Extract the extra size bits
            val extraBits = nextByte and CONTINUATION_SIZE_MASK
            size = (extraBits shl usedBits) or size

            // Check if current byte asks for extra number information
            needsExtra = extra(nextByte)
            usedBits += CONTINUATION_SIZE_BIT_COUNT
        }

        return size to ObjectType.fromValue(typeValue)
    }

    data class ReferenceLine(val hash: Hash, val name: String, val capabilities: List<String>)

    private fun ByteArrayConsumer.consumeRefsPckLines(): List<ReferenceLine> = buildList {
        val lengthSize = 4

        fun peekLenght(consumer: ByteArrayConsumer): Int {
            return consumer.peek(lengthSize).toInt()
        }

        val start = String(consume(peekLenght(this@consumeRefsPckLines), lengthSize))
        val flush = consume(4).toInt()

        check(start.startsWith("# service=git-upload-pack"))
        check(flush == 0)

        do {
            try {
                val lineLenght = peekLenght(this@consumeRefsPckLines)
                if (lineLenght == 0) {
                    return@buildList
                } else {
                    val line = String(this@consumeRefsPckLines.consume(lineLenght, lengthSize))
                        .trim()
                        .split(' ', limit = 2)
                        .map {
                            it.split(NULL_CHAR, limit = 2)
                        }
                        .flatten()
                    add(
                        ReferenceLine(
                            Hash(line[0]),
                            line[1],
                            line.getOrNull(2)?.split(" ") ?: emptyList()
                        )
                    )
                }
            } catch (e: IllegalArgumentException) {
                System.err.println("Didn't correctly consume everything.")
                return@buildList
            } catch (e: IllegalStateException) {
                System.err.println(e.message)
                return@buildList
            }
        } while (true)
    }

    fun HttpRequestBuilder.url(baseUrl: String, block: URLBuilder.() -> Unit) {
        val urlBuilder = URLBuilder(baseUrl)
        urlBuilder.block()
        url(urlBuilder.buildString())
    }
}
