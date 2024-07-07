package jarrid.keyper.cli.data

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required

import jarrid.keyper.cli.DataSubcommand
import jarrid.keyper.key.data.Decrypt
import kotlinx.coroutines.runBlocking

class Decrypt : DataSubcommand(help = "Decrypt data with using key") {
    private val ciphertext: String by option(help = "The Base64 encoded ciphertext data to decrypt").required()

    override fun run() = runBlocking {
        val decryptor = Decrypt(backend, stack, deploymentId, keyId)
        val decrypted = decryptor.run(ciphertext)
        logger.info("Decrypted value: $decrypted")
    }
}