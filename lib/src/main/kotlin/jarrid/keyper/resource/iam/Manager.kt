package jarrid.keyper.resource.iam

import io.klogging.Klogging
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Payload
import jarrid.keyper.resource.ResourceIsUndefinedException
import jarrid.keyper.resource.Stack
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.model.NewUUID
import jarrid.keyper.resource.Manager as ResourceManager


class Manager(
    backend: Backend,
    stack: Stack
) : ResourceManager(backend = backend, stack = stack), Klogging {

    suspend fun createRole(payload: Payload): Model {
        // Determine the deployment
        val deployment = payload.deployment?.let {
            backend.getDeployment(Deployment.get(it.id ?: NewUUID.getEmpty(), it.name ?: "default"))
        } ?: backend.getDeployment()

        // Assert resource is defined
        val resource = payload.resource
            ?: throw ResourceIsUndefinedException()
        val name = resource.name ?: throw RoleIsUndefinedException()

        // Determine the role resource
        val role = Model(
            id = resource.id ?: NewUUID.get(),
            name = name,
        )

        backend.write(role, deployment)
        return role
    }
}