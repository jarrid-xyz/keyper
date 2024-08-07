package jarrid.keyper.cli.resource.key

import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import jarrid.keyper.cli.BaseSubcommand
import jarrid.keyper.resource.Deployment
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.model.toUUID
import kotlinx.coroutines.runBlocking
import java.util.*
import jarrid.keyper.resource.key.Model as Key


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

    private lateinit var useBackend: Backend
    lateinit var useDeployment: Deployment
    lateinit var key: Key

    private suspend fun getKey(keyId: UUID?, keyName: String?): Key {
        // Ensure that at least one of keyId or keyName is specified
        require(!(keyId == null && keyName == null)) {
            "Either --key-id or --key-name must be specified."
        }
        val keys = useBackend.getResources<Key>(useDeployment)
        val key = keys.find { key ->
            (keyId != null && key.base.id == keyId) || (keyName != null && key.base.name == keyName)
        } ?: throw IllegalArgumentException("Key not found with the specified ID or name.")
        return key
    }

    override fun run() {
        useBackend = backend.get()
        useDeployment = useBackend.getDeployment(
            Deployment.get(name = deployment ?: "default")
        )
        runBlocking {
            key = getKey(keyId, keyName)
            runAsync()
        }
    }

    abstract suspend fun runAsync()
}