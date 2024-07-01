package jarrid.keyper.cli

import com.github.ajalt.clikt.core.subcommands
import jarrid.keyper.cli.deploy.Apply
import jarrid.keyper.cli.deploy.Plan
import jarrid.keyper.utils.logging.config

fun main(args: Array<String>) {
    config()
    Keyper()
        .subcommands(
            Key(),
            Deploy().subcommands(
                Plan(),
                Apply()
            )
        ).main(args)
}
