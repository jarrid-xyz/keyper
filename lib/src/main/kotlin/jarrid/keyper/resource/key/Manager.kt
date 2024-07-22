package jarrid.keyper.resource.key

import io.klogging.Klogging
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Payload
import jarrid.keyper.resource.ResourceIsUndefinedException
import jarrid.keyper.resource.Stack
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.model.NewTimestamp
import jarrid.keyper.utils.model.NewUUID
import jarrid.keyper.resource.Manager as ResourceManager

class Manager(
    backend: Backend,
    stack: Stack
) : ResourceManager(backend = backend, stack = stack), Klogging {

    suspend fun createKey(payload: Payload): Model {
        // Determine the deployment
        val deployment = payload.deployment?.let {
            backend.getDeployment(Deployment.get(it.id ?: NewUUID.getEmpty(), it.name ?: "default"))
        } ?: backend.getDeployment()

        // Assert resource is defined
        val resource = payload.resource
            ?: throw ResourceIsUndefinedException()

        // Determine the key resource
        val key = Model(
            id = resource.id ?: NewUUID.get(),
            name = resource.name,
            ttl = resource.getConfigAttribute("ttl", 7) as Int
        )
        key.base.created = NewTimestamp.get()


        backend.write(key, deployment)
        return key
    }
}