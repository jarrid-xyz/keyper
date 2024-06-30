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

interface Manager {
    companion object {
        @JvmStatic
        suspend fun convert(payload: Payload, usage: Usage, backend: Backend): Model {
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

                Usage.ENCRYPT_DATA -> {
                    TODO()
                }
            }
        }

        @JvmStatic
        suspend fun run(payload: Payload, usage: Usage, backend: Backend): Model {
            val config: Model = convert(payload, usage, backend)
            backend.write(config)
            return config
        }
    }

    suspend fun createKey(): Model
    suspend fun shareKey()
    suspend fun encrypt()
    suspend fun getKey()
    suspend fun decrypt()
}
