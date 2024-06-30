package jarrid.keyper.cli

import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import jarrid.keyper.api.KeyServiceImpl
import jarrid.keyper.api.Options
import jarrid.keyper.key.Usage
import kotlinx.coroutines.runBlocking

class Key :
    BaseSubcommand(help = "Key module generates key configs in json based on usage specified. For more information, visit: https://jarrid.xyz") {

    private val usage: Usage by option(
        help = "Specify usage"
    ).enum<Usage>().required()

    private val context: Map<String, String> by option(
        "-c", "--context",
        help = "Provide additional context as key:value map"
    ).associate()

    override fun run() = runBlocking {
        logger.info(
            "Running Key command with the following options: " +
                    "Usage: $usage, Backend: $backend, Stack: $stack, " +
                    "Context: $context"
        )
        runAsync()
    }

    private suspend fun runAsync() {
        val service = KeyServiceImpl(
            options = Options(
                stack = stack,
                backend = backend
            )
        )
        when (usage) {
            Usage.CREATE_KEY -> service.createSymmetricKey(context)
            else -> TODO()
        }
    }
}