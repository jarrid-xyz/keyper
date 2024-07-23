package jarrid.keyper.resource

import io.klogging.Klogging
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.model.NewTimestamp
import jarrid.keyper.utils.model.NewUUID

open class Manager(
    val backend: Backend,
    val stack: Stack
) : Klogging {

    fun getOrCreateDeployment(payload: Payload): Deployment {
        return payload.deployment?.let {
            backend.getDeployment(Deployment.get(it.id ?: NewUUID.getEmpty(), it.name ?: "default"))
        } ?: backend.getDeployment()
    }

    suspend fun createDeployment(payload: Payload): Deployment {
        val deployment = Deployment.new(
            id = payload.deployment?.id,
            name = payload.deployment?.name ?: "default",
            context = payload.deployment?.context
        )
        deployment.base.created = NewTimestamp.get()

        backend.createDeploymentDir(deployment)
        backend.write(deployment)
        return deployment
    }
}