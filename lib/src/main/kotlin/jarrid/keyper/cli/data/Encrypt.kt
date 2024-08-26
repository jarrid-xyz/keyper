package jarrid.keyper.cli.data

import com.github.ajalt.clikt.parameters.options.option
import jarrid.keyper.cli.resource.key.InputValidationException
import jarrid.keyper.cli.resource.key.KeySubcommand
import jarrid.keyper.resource.Model

class Encrypt : KeySubcommand(help = "Encrypt data using a key") {
    private val plaintext: String? by option(help = "The plaintext data to encrypt")

    override suspend fun runAsync() {
        validate(plaintext)
        val payload = Model(resource = key, deployment = useDeployment)
        val encryptor = getEncryptor(payload)

        val encrypted = when {
            plaintext != null -> encryptor.run(
                plaintext!!, outputPath,
                base64DecodeRead = false,
                base64EncodeWrite = true
            )

            inputPath != null -> encryptor.run(
                inputPath!!, outputPath,
                base64DecodeRead = false,
                base64EncodeWrite = true
            )

            else -> throw InputValidationException("Invalid input") // This should never happen due to the previous checks
        }

        if (encrypted == null) {
            logger.info("Write encrypted data to file $outputPath")
        } else {
            echo("Encrypted value: $encrypted")
        }
    }
}