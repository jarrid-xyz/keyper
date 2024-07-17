package jarrid.keyper.resource.key.data

import io.klogging.Klogging
import jarrid.keyper.resource.Backend
import jarrid.keyper.resource.Config
import jarrid.keyper.resource.Stack
import jarrid.keyper.resource.key.Model
import java.util.*

abstract class Base(
    val backend: Backend,
    val stack: Stack,
    val deploymentId: UUID?,
    val keyId: UUID
) : Klogging {

    private val appConfig = Config().get()
    private val provider = stack.getConfig(appConfig)!!
    private val useBackend = backend.get()
    val projectId = provider.accountId
    val region = provider.region

    private suspend fun getDeploymentId(): UUID {
        return useBackend.getOrCreateDeploymentId(deploymentId, force = false)
            ?: throw IllegalArgumentException("DeploymentId not created or not found")
    }


    suspend fun getKeyConfig(): Model {
        val deploymentId = getDeploymentId()
        val keyConfig = useBackend.getConfig(deploymentId = deploymentId, keyId = keyId)
        return keyConfig
    }
}