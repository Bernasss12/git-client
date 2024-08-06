package commands

import kotlinx.cli.ArgType
import kotlinx.cli.ExperimentalCli
import kotlinx.cli.Subcommand
import kotlinx.cli.required
import java.nio.file.Path

@OptIn(ExperimentalCli::class)
object CatFile : Subcommand("cat-file", "") {
    val path by option(ArgType.String, "path", "p", "Path to blob file").required()

    override fun execute() {

    }
}