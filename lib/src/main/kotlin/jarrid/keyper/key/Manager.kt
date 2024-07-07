package jarrid.keyper.key

import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.model.NewTimestamp
import jarrid.keyper.utils.model.NewUUID
import java.util.*

data class Payload(
    val keyId: UUID? = null,
    val deploymentId: UUID? = null,
    val context: Map<String, Any>? = null
)

abstract class Manager(backend: Backend) {
    open val backend: Backend = backend
    suspend fun convert(payload: Payload, usage: Usage): Model {
        when (usage) {
            Usage.CREATE_KEY -> {
                return Model(
                    usage = usage,
                    keyId = NewUUID.get(),
                    created = NewTimestamp.get(),
                    deploymentId = backend.getOrCreateDeploymentId(payload.deploymentId),
                    ttl = 7,
                    context = payload.context,
                )
            }

            Usage.SHARE_KEY -> {
                TODO()
            }

            Usage.ENCRYPT -> {
                TODO()
            }

            Usage.DECRYPT -> {
                TODO()
            }
        }
    }

    suspend fun run(payload: Payload, usage: Usage): Model {
        val config: Model = convert(payload, usage)
        backend.write(config)
        return config
    }

    abstract suspend fun createKey(): Model
    abstract suspend fun shareKey()
    abstract suspend fun encrypt()
    abstract suspend fun getKey()
    abstract suspend fun decrypt()
}
