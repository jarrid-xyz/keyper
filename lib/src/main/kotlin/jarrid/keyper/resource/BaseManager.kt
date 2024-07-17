package jarrid.keyper.resource
import jarrid.keyper.resource.key.Model
import jarrid.keyper.utils.file.Backend
import java.util.UUID

abstract class BaseManager(backend: Backend) {
    val backend=backend

    suspend fun getDeploymentId(deploymentId: UUID?): UUID? {
        return backend.getOrCreateDeploymentId(deploymentId)
    }
}
