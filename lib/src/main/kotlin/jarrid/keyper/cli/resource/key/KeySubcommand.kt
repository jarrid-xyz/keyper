package jarrid.keyper.cli.resource.key

import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import jarrid.keyper.cli.BaseSubcommand
import jarrid.keyper.utils.model.toUUID
import kotlinx.coroutines.runBlocking
import java.util.*

abstract class KeySubcommand(help: String = "") : BaseSubcommand(help = help) {
    val deployment: String? by option(
        "-d", "--deployment", help = "Deployment name"
    ).default("default")

    val keyId: UUID by option(
        "-k", "--key-id", help = "keyId to use",
    ).convert { it.toUUID() }.required()

    override fun run() {
        runBlocking {
            runAsync()
        }
    }

    abstract suspend fun runAsync()
}