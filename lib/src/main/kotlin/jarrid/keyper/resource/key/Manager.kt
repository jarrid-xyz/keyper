package jarrid.keyper.resource.key

import io.klogging.Klogging
import jarrid.keyper.app.Stack
import jarrid.keyper.resource.*
import jarrid.keyper.utils.file.Backend
import java.util.*
import jarrid.keyper.resource.Manager as ResourceManager
import jarrid.keyper.resource.key.Model as Key

class KeyNotFoundException(val name: String, message: String = "Key: $name not found") : Exception(message)
class MultipleKeysFoundException(val name: String, message: String = "Multiple keys found by $name") :
    Exception(message)

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

    suspend fun getKey(keyId: UUID?, keyName: String?, deployment: Deployment): Key {
        require(!(keyId == null && keyName == null)) {
            "Either --key-id or --key-name must be specified."
        }
        // Ensure that at least one of keyId or keyName is specified
        require(!(keyId == null && keyName == null)) {
            "Either --key-id or --key-name must be specified."
        }
        val keys = backend.getResources<Key>(deployment)
        val matched = keys.filter { key ->
            (keyId == null || key.base.id == keyId) && (keyName == null || key.base.name == keyName)
        }
        return when {
            matched.isEmpty() -> throw KeyNotFoundException("Id: $keyId, Name: $keyName")
            matched.size > 1 -> throw MultipleKeysFoundException("ID: $keyId, Name: $keyName")
            else -> matched.first()
        }
    }

}