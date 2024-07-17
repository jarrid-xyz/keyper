package jarrid.keyper.cli.data

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required

import jarrid.keyper.cli.DataSubcommand
import jarrid.keyper.resource.key.data.Encrypt
import kotlinx.coroutines.runBlocking

class Encrypt : DataSubcommand(help = "Encrypt data with using key") {
    private val plaintext: String by option(help = "The plaintext data to encrypt").required()

    override fun run() = runBlocking {
        val encryptor = Encrypt(backend, stack, deploymentId, keyId)
        val encrypted = encryptor.run(plaintext)
        logger.info("Encrypted value: $encrypted")
    }
}