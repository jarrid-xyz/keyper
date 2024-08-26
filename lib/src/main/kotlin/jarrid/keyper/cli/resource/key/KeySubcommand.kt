package jarrid.keyper.cli.resource.key

import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import jarrid.keyper.app.Stack
import jarrid.keyper.cli.BaseSubcommand
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Model
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.model.toUUID
import kotlinx.coroutines.runBlocking
import java.io.File
import java.util.*
import jarrid.keyper.resource.key.Manager as KeyManager
import jarrid.keyper.resource.key.Model as Key
import jarrid.keyper.resource.key.data.Base as EncryptorDecryptor
import jarrid.keyper.resource.key.data.aws.Decrypt as AwsDecrypt
import jarrid.keyper.resource.key.data.aws.Encrypt as AwsEncrypt
import jarrid.keyper.resource.key.data.gcp.Decrypt as GcpDecrypt
import jarrid.keyper.resource.key.data.gcp.Encrypt as GcpEncrypt

class InputValidationException(message: String = "") : Exception(message)

// Custom exception for unsupported stack type
class UnsupportedStackException(message: String = "") : Exception(message)


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

    // Function to return the correct encryptor based on the stack
    fun getEncryptor(payload: Model): EncryptorDecryptor {
        return when (stack) {
            Stack.GCP -> GcpEncrypt(backend, stack, payload)
            Stack.AWS -> AwsEncrypt(backend, stack, payload)
            else -> throw UnsupportedStackException("Unsupported stack type: $stack")
        }
    }

    fun getDecryptor(payload: Model): EncryptorDecryptor {
        return when (stack) {
            Stack.GCP -> GcpDecrypt(backend, stack, payload)
            Stack.AWS -> AwsDecrypt(backend, stack, payload)
            else -> throw IllegalArgumentException("Unsupported stack type: $stack")
        }
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