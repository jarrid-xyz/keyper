package jarrid.keyper.cli


import com.github.ajalt.clikt.core.subcommands
import jarrid.keyper.utils.logging.config

fun main(args: Array<String>) {
    config()
    Keyper()
        .subcommands(
            Key(),
            Deploy()
        ).main(args)
}


