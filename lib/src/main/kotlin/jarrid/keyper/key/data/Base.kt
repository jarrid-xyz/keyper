package jarrid.keyper.key.data

import io.klogging.Klogging
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Config
import jarrid.keyper.app.Stack
import jarrid.keyper.key.Model
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