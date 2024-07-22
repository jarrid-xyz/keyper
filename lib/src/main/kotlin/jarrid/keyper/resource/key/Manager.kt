package jarrid.keyper.resource.key

import io.klogging.Klogging
import jarrid.keyper.resource.Payload
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.model.NewUUID

class Manager(
    val backend: Backend
) : Klogging {
    private fun convert(payload: Payload): Model {
        val ttl = payload.getConfigAttribute("ttl") as? Int ?: 7
        val out = Model(
            ttl = ttl,
            id = NewUUID.get(),
            name = payload.base.name,
            context = payload.base.context,
        )
        return out
    }

    suspend fun createKey(payload: Payload): Model {
        val resource: Model = convert(payload)
        val deployment = backend.getDeployment()
        backend.write(resource, deployment)
        return resource
    }
}