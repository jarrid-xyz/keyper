package jarrid.keyper.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import jarrid.keyper.cli.key.Action
import kotlinx.coroutines.runBlocking
import java.util.*

abstract class KeySubcommand(help: String = "") :
    BaseSubcommand(help = help) {
    abstract val action: Action

    val deploymentName: String? by option(
        help = "Deployment name"
    )
    val deploymentId: UUID? by option(
        help = "Deployment id"
    ).convert { UUID.fromString(it) }

    val context: Map<String, String> by option(
        "-c", "--context",
        help = "Provide additional context as key:value map"
    ).associate()

    override fun run() = runBlocking {
        logger.info(
            "Running Key command with the following options: " +
                    "action: $action, Backend: $backend, " +
                    "Stack: $stack, Context: $context"
        )
        runAsync()
    }

    abstract suspend fun runAsync()

}

class Key :
    NoOpCliktCommand(
        help = "Key module allow you to create, update, delete and list keys. For more information, visit: https://jarrid.xyz"
    )
