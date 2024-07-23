package jarrid.keyper.resource

import io.klogging.Klogging
import jarrid.keyper.utils.file.Backend
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

    suspend fun createDeployment(payload: BasePayload): Deployment {
        val deployment = Deployment.create(payload)
        backend.createDeploymentDir(deployment)
        backend.write(deployment)
        return deployment
    }
}