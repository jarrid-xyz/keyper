package jarrid.keyper.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import jarrid.keyper.utils.model.toUUID
import java.util.*


abstract class DataSubcommand(help: String = "") : BaseSubcommand(help = help) {

    val deployment: String? by option(
        "-d", "--deployment", help = "Deployment name"
    ).default("default")

    val keyId: UUID by option(
        "-k", "--key-id", help = "keyId to use",
    ).convert { it.toUUID() }.required()
}

class Data :
    NoOpCliktCommand(help = "Data module to encrypt and decrypt data. For more information, visit: https://jarrid.xyz")
