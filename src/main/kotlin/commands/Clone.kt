@file:OptIn(ExperimentalCli::class)

package commands

import Remote
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.coroutines.runBlocking

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

        */

        val bytes = runBlocking {
            /*// this is correctly requesting the list of refs!
            val response = HTTP_CLIENT.get {
                url("https://github.com/Bernasss12/BetterEnchantedBooks") {
                    appendEncodedPathSegments("info", "refs")
                    parameters.append(
                        name = "service",
                        value = "git-upload-pack"
                    )
                }
            }*/

            val remote = "https://github.com/Bernasss12/BetterEnchantedBooks"
            val references = Remote.fetchReferences(remote)
            val result = Remote.fetchPackFile(remote, references)
            println(result)

            // Step 2: Create and send a POST request to git-upload-pack
//            val packRequestBody = "0032want ${ targetHash }\n00000009done\n"
            /*
            val packResponse = HTTP_CLIENT.post {
                url("https://github.com/Bernasss12/BetterEnchantedBooks") {
                    appendEncodedPathSegments("git-upload-pack")
                }
                headers {
                    append(HttpHeaders.ContentType, "application/x-git-upload-pack-request")
                    append(HttpHeaders.Accept, "application/x-git-upload-pack-result")
                }
                setBody(packRequestBody.toByteArray(StandardCharsets.UTF_8))
            }

            packResponse.body<ByteArray>()
            */
        }

//        println(String(bytes))
    }
}

