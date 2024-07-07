package jarrid.keyper.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import jarrid.keyper.utils.model.toUUID
import java.util.*


abstract class DataSubcommand(help: String = "") : BaseSubcommand(help = help) {
    val keyId: UUID by option(help = "keyId to use to encrypt data")
        .convert { it.toUUID() }.required()
    val deploymentId: UUID? by option(help = "deploymentId of the key to use to encrypt data")
        .convert { it.toUUID() }
}

class Data :
    NoOpCliktCommand(help = "Data module to encrypt and decrypt data. For more information, visit: https://jarrid.xyz")
