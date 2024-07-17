package jarrid.keyper.cli.key

import jarrid.keyper.cli.KeySubcommand
import jarrid.keyper.resource.key.Usage
import kotlinx.coroutines.runBlocking

class Create(help: String = "Create key") : KeySubcommand(help = help) {
    override val usage = Usage.CREATE_KEY

    override fun run() = runBlocking {
        logger.info(
            "Running Key command with the following options: " +
                    "Usage: $usage, Backend: $backend, Stack: $stack, " +
                    "Context: $context"
        )
        runAsync()
    }
}