package jarrid.keyper.cli.data

import com.github.ajalt.clikt.parameters.options.option
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.cli.resource.key.InputValidationException
import jarrid.keyper.cli.resource.key.KeySubcommand
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.data.Encrypt


class Encrypt : KeySubcommand(help = "Encrypt data with using key") {
    private val plaintext: String? by option(help = "The plaintext data to encrypt")

    override suspend fun runAsync() {
        validate(plaintext)
        val payload = Model(resource = key, deployment = useDeployment)
        val encryptor = getEncryptor(backend, stack, payload)

        val encrypted = when {
            plaintext != null -> encryptor.run(plaintext!!)
            inputPath != null -> encryptor.run(inputPath!!)
            else -> throw InputValidationException("Invalid input") // This should never happen due to the previous checks
        }

        if (outputPath != null) {
            outputPath!!.writeText(encrypted)
        } else {
            echo("Encrypted value: $encrypted")
        }
    }

    fun getEncryptor(backend: Backend, stack: Stack, payload: Model): Encrypt {
        return Encrypt(backend, stack, payload)
    }

}