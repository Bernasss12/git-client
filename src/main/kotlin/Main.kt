@file:OptIn(ExperimentalCli::class)

import commands.CatFile
import commands.HashObject
import commands.Init
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

fun main(args: Array<String>) {
    val parser = ArgParser("example", strictSubcommandOptionsOrder = true)
    parser.subcommands(Init, CatFile, HashObject)
    parser.parse(args)
}
