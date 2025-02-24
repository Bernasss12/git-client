@file:OptIn(ExperimentalCli::class)

import commands.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ExperimentalCli

fun main(args: Array<String>) {
    val parser = ArgParser("git", strictSubcommandOptionsOrder = true)
    parser.subcommands(Init, CatFile, HashObject, LsTree, WriteTree, CommitTree, Clone)
    parser.parse(args)
}

fun debug(string: String) {
    System.err.println(string)
}
