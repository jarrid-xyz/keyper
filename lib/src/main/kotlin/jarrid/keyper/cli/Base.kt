package jarrid.keyper.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.enum
import io.klogging.Klogging
import jarrid.keyper.resource.Backend
import jarrid.keyper.resource.Stack
import jarrid.keyper.tfcdk.KeyStack
import kotlin.reflect.KClass
import jarrid.keyper.utils.file.Backend as FileBackend

abstract class BaseSubcommand(help: String = "") : CliktCommand(help), Klogging {
    val backend: Backend by option(
        help = "Specify configuration backend"
    ).enum<Backend>()
        .default(Backend.LOCAL)

    val stack: Stack by option(
        help = "Specify deployment stack"
    ).enum<Stack>().default(Stack.GCP)

    val deployment: String? by option(
        help = "Deployment name"
    ).default("default")

    suspend fun runTf() {
        val backend: FileBackend = backend.get()
        val stack: KClass<out KeyStack> = stack.get()
        val tf = jarrid.keyper.tfcdk.Stack(backend, stack)
        tf.run()
    }
}

class Keyper :
    NoOpCliktCommand(help = "Keyper is a suite of key manage APIs to simplify key creation, management, encryption/decryption in standardized and secured way. For more information, visit: https://jarrid.xyz")
