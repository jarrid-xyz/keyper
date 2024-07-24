package jarrid.keyper.cli.data

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import jarrid.keyper.cli.DataSubcommand
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.data.Encrypt
import kotlinx.coroutines.runBlocking
import jarrid.keyper.resource.key.Model as Key

class Encrypt : DataSubcommand(help = "Encrypt data with using key") {
    private val plaintext: String by option(help = "The plaintext data to encrypt").required()

    override fun run() = runBlocking {
        val deployment = backend.get().getDeployment(
            Deployment.get(name = deployment ?: "default")
        )
        val key = Key(id = keyId)
        val payload = Model(resource = key, deployment = deployment)
        val encryptor = Encrypt(backend, stack, payload)
        val encrypted = encryptor.run(plaintext)
        echo("Encrypted value: $encrypted")
    }
}