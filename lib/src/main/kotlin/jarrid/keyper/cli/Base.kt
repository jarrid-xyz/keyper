package jarrid.keyper.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.output.MordantHelpFormatter
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import io.klogging.Klogging
import jarrid.keyper.resource.Backend
import jarrid.keyper.resource.Stack

abstract class BaseSubcommand(help: String = "", name: String? = null) : CliktCommand(help = help, name = name),
    Klogging {
    val backend: Backend by option(
        "-b", "--backend", help = "Specify configuration backend"
    ).enum<Backend>().default(Backend.LOCAL)

    val stack: Stack by option(
        "-s", "--stack", help = "Specify deployment stack"
    ).enum<Stack>().default(Stack.GCP)
}

open class Keyper(
    help: String = "Keyper is a suite of key manage APIs to simplify key creation, management, " +
            "encryption/decryption in standardized and secured way. " +
            "For more information, visit: https://jarrid.xyz/keyper"
) :
    NoOpCliktCommand(help) {
    init {
        context {
            helpFormatter = {
                MordantHelpFormatter(it, showDefaultValues = true, showRequiredTag = true)
            }
        }
    }
}
