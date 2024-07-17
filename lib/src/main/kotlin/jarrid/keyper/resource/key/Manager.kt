package jarrid.keyper.resource.key

import io.klogging.Klogging
import jarrid.keyper.resource.BaseManager
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.model.NewTimestamp
import jarrid.keyper.utils.model.NewUUID
import java.util.*

data class Payload(
    val keyId: UUID? = null,
    val deploymentId: UUID? = null,
    val context: Map<String, Any>? = null
)

class Manager(
    private val payload: Payload,
    backend: Backend
) : Klogging, BaseManager(backend = backend) {

    suspend fun convert(payload: Payload, usage: Usage): Model {
        when (usage) {
            Usage.CREATE_KEY -> {
                return Model(
                    usage = usage,
                    keyId = NewUUID.get(),
                    created = NewTimestamp.get(),
                    deploymentId = getDeploymentId(payload.deploymentId),
                    ttl = 7,
                    context = payload.context,
                )
            }

            Usage.SHARE_KEY,
            Usage.ENCRYPT,
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

    suspend fun createKey(): Model {
        return run(payload, Usage.CREATE_KEY)
    }

    suspend fun shareKey() {
        TODO()
    }

    suspend fun getKey() {
        TODO()
    }

    suspend fun encrypt() {
        TODO()
    }

    suspend fun decrypt() {
        TODO()
    }
}