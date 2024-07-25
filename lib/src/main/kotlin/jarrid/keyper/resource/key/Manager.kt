package jarrid.keyper.resource.key

import io.klogging.Klogging
import jarrid.keyper.resource.*
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.resource.Manager as ResourceManager
import jarrid.keyper.resource.key.Model as Key

class Manager(
    backend: Backend,
    stack: Stack
) : ResourceManager(backend = backend, stack = stack), Klogging {

    suspend fun createKey(payload: Payload): Key {
        // Determine the deployment
        val deployment = getOrCreateDeployment(payload)

        // Assert resource is defined
        val resource = payload.resource
            ?: throw ResourceIsUndefinedException()

        // Determine the key resource
        val key = Key.create(resource)
        backend.write(key, deployment)
        return key
    }

    suspend fun list(payload: Payload): List<Key> {
        // Determine the deployment
        val deployment = getOrCreateDeployment(payload)
        return backend.getResources(deployment)
    }

    suspend fun permission(payload: Payload, type: EditPermission, roles: List<String>) {
        val deployment: Deployment = getOrCreateDeployment(payload)
        val checked: Resource = backend.getResourceWithCheck(payload, type = ResourceType.KEY)
        val key = backend.getResource<Key>(checked, deployment)
        key.editPermission(type, roles)
        backend.write(key, deployment)
    }
}