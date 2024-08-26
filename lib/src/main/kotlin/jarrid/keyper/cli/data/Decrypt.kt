package jarrid.keyper.cli.data

import com.github.ajalt.clikt.parameters.options.option
import jarrid.keyper.cli.resource.key.InputValidationException
import jarrid.keyper.cli.resource.key.KeySubcommand
import jarrid.keyper.resource.Model

class Decrypt : KeySubcommand(help = "Decrypt data using a key") {
    private val ciphertext: String? by option(help = "The Base64 encoded ciphertext data to decrypt")

    override suspend fun runAsync() {
        validate(ciphertext)
        val payload = Model(resource = key, deployment = useDeployment)
        val decryptor = getDecryptor(payload)

        val decrypted = when {
            ciphertext != null -> decryptor.run(
                ciphertext!!,
                outputPath,
                base64DecodeRead = true,
                base64EncodeWrite = false
            )

            inputPath != null -> decryptor.run(
                inputPath!!,
                outputPath,
                base64DecodeRead = true,
                base64EncodeWrite = false
            )

            else -> throw InputValidationException("Invalid input") // This should never happen due to the previous checks
        }

        if (decrypted == null) {
            logger.info("Write decrypted data to file $outputPath")
        } else {
            echo("Decrypted value: $decrypted")
        }
    }
}