package jarrid.keyper.cli.data

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import jarrid.keyper.cli.DataSubcommand
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.data.Decrypt
import kotlinx.coroutines.runBlocking
import jarrid.keyper.resource.key.Model as Key

class Decrypt : DataSubcommand(help = "Decrypt data with using key") {
    private val ciphertext: String by option(help = "The Base64 encoded ciphertext data to decrypt").required()

    override fun run() = runBlocking {
        val deployment = backend.get().getDeployment(
            Deployment.get(name = deployment ?: "default")
        )
        val key = Key(id = keyId)
        val payload = Model(resource = key, deployment = deployment)
        val decryptor = Decrypt(backend, stack, payload)
        val decrypted = decryptor.run(ciphertext)
        echo("Decrypted value: $decrypted")
    }
}