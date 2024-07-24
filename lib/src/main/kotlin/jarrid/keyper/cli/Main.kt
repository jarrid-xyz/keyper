package jarrid.keyper.cli

import com.github.ajalt.clikt.core.subcommands
import jarrid.keyper.cli.data.Decrypt
import jarrid.keyper.cli.data.Encrypt
import jarrid.keyper.cli.deploy.Apply
import jarrid.keyper.cli.deploy.Destroy
import jarrid.keyper.cli.deploy.Plan
import jarrid.keyper.cli.resource.Create
import jarrid.keyper.cli.resource.List
import jarrid.keyper.utils.logging.config
import jarrid.keyper.cli.deploy.Create as CreateDeploy

fun main(args: Array<String>) {
    config()
    Keyper()
        .subcommands(
            Resource().subcommands(
                Create(),
                List()
            ),
            Deploy().subcommands(
                CreateDeploy(),
                Plan(),
                Apply(),
                Destroy()
            ),
            Data().subcommands(
                Encrypt(),
                Decrypt()
            )
        ).main(args)
}
