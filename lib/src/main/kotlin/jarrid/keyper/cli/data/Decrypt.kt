package jarrid.keyper.cli.data

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.cli.resource.key.KeySubcommand
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.data.Decrypt

class Decrypt : KeySubcommand(help = "Decrypt data with using key") {
    private val ciphertext: String by option(help = "The Base64 encoded ciphertext data to decrypt").required()

    override suspend fun runAsync() {
        val payload = Model(resource = key, deployment = useDeployment)
        val decryptor = getDecryptor(backend, stack, payload)
        val decrypted = decryptor.run(ciphertext)
        echo("Decrypted value: $decrypted")
    }

    fun getDecryptor(backend: Backend, stack: Stack, payload: Model): Decrypt {
        return Decrypt(backend, stack, payload)
    }
}