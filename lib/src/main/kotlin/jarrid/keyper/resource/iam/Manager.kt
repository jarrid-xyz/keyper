package jarrid.keyper.resource.iam

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

    suspend fun createRole(payload: Payload): Model {
        // Determine the deployment
        val deployment = getOrCreateDeployment(payload)

        // Assert resource is defined
        val resource = payload.resource
            ?: throw ResourceIsUndefinedException()

        // Determine the role resource
        val role = Model.create(resource)

        backend.write(role, deployment)
        return role
    }

    suspend fun list(payload: Payload): List<Model> {
        // Determine the deployment
        val deployment = getOrCreateDeployment(payload)
        return backend.getResources(deployment)
    }
}