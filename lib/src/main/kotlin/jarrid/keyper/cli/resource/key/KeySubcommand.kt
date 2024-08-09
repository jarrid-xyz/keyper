package jarrid.keyper.cli.resource.key

import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import jarrid.keyper.cli.BaseSubcommand
import jarrid.keyper.resource.Deployment
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.model.toUUID
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import jarrid.keyper.resource.key.Manager as KeyManager
import jarrid.keyper.resource.key.Model as Key

class InputValidationException(message: String = "") : Exception(message)


abstract class KeySubcommand(help: String = "") : BaseSubcommand(help = help) {
    val deployment: String? by option(
        "-d", "--deployment", help = "Deployment name"
    ).default("default")

    val keyId: UUID? by option(
        "-k", "--key-id", help = "Key ID to use"
    ).convert { it.toUUID() }

    private val keyName: String? by option(
        "-n", "--key-name", help = "Key name to use"
    )

    val inputPath: File? by option(
        "--input-path",
        help = "The input file path containing plaintext data to encrypt"
    ).file()
    val outputPath: File? by option(
        "--output-path",
        help = "The output file path containing ciphertext"
    ).file()

    private lateinit var useBackend: Backend
    lateinit var useDeployment: Deployment
    private lateinit var keyManager: KeyManager
    lateinit var key: Key

    fun getKeyManager(): KeyManager {
        return KeyManager(backend = useBackend, stack = stack)
    }

    override fun run() {
        useBackend = backend.get()
        useDeployment = useBackend.getDeployment(
            Deployment.get(name = deployment ?: "default")
        )
        keyManager = getKeyManager()
        runBlocking {
            key = keyManager.getKey(keyId, keyName, useDeployment)
            runAsync()
        }
    }

    fun validate(input: String?) {
        if (input != null && inputPath != null) {
            throw InputValidationException("You must specify either plaintext or filePath, not both")
        }

        if (input == null && inputPath == null) {
            throw InputValidationException("You must specify either plaintext or filePath")
        }
    }

    abstract suspend fun runAsync()
}