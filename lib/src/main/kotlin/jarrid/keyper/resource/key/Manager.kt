package jarrid.keyper.resource.key

import io.klogging.Klogging
import jarrid.keyper.resource.Payload
import jarrid.keyper.resource.ResourceIsUndefinedException
import jarrid.keyper.resource.Stack
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.resource.Manager as ResourceManager

class Manager(
    backend: Backend,
    stack: Stack
) : ResourceManager(backend = backend, stack = stack), Klogging {

    suspend fun createKey(payload: Payload): Model {
        // Determine the deployment
        val deployment = getOrCreateDeployment(payload)

        // Assert resource is defined
        val resource = payload.resource
            ?: throw ResourceIsUndefinedException()

        // Determine the key resource
        val key = Model.create(resource)
        backend.write(key, deployment)
        return key
    }
}