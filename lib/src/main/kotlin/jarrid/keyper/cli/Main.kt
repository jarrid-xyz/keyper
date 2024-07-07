package jarrid.keyper.cli

import com.github.ajalt.clikt.core.subcommands
import jarrid.keyper.cli.data.Encrypt
import jarrid.keyper.cli.deploy.Apply
import jarrid.keyper.cli.deploy.Destroy
import jarrid.keyper.cli.deploy.Plan
import jarrid.keyper.cli.key.Create
import jarrid.keyper.cli.key.List
import jarrid.keyper.utils.logging.config

fun main(args: Array<String>) {
    config()
    Keyper()
        .subcommands(
            Key().subcommands(
                Create(),
                List()
            ),
            Deploy().subcommands(
                Plan(),
                Apply(),
                Destroy()
            ),
            Data().subcommands(
                Encrypt()
            )
        ).main(args)
}
