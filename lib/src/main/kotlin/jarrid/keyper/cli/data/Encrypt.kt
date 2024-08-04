package jarrid.keyper.cli.data

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.cli.resource.key.KeySubcommand
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.data.Encrypt
import jarrid.keyper.resource.key.Model as Key


class Encrypt : KeySubcommand(help = "Encrypt data with using key") {
    private val plaintext: String by option(help = "The plaintext data to encrypt").required()

    override suspend fun runAsync() {
        val deployment = backend.get().getDeployment(
            Deployment.get(name = deployment ?: "default")
        )
        val key = Key(id = keyId)
        val payload = Model(resource = key, deployment = deployment)
        val encryptor = getEncryptor(backend, stack, payload)
        val encrypted = encryptor.run(plaintext)
        echo("Encrypted value: $encrypted")
    }

    fun getEncryptor(backend: Backend, stack: Stack, payload: Model): Encrypt {
        return Encrypt(backend, stack, payload)
    }
}