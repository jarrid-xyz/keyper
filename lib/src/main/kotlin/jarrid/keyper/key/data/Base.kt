package jarrid.keyper.key.data

import io.klogging.Klogging
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Config
import jarrid.keyper.app.Stack
import jarrid.keyper.key.Model
import java.util.*

abstract class Base(backend: Backend, stack: Stack, deploymentId: UUID?, keyId: UUID) : Klogging {

    val keyId: UUID = keyId
    val backend: Backend = backend
    val stack: Stack = stack

    private val appConfig = Config().get()
    private val provider = stack.getConfig(appConfig)!!
    private val useBackend = backend.get()
    private val deploymentId: UUID? = deploymentId
    val projectId = provider.accountId
    val region = provider.region

    private suspend fun getDeploymentId(): UUID {
        return useBackend.getOrCreateDeploymentId(byDeploymentId = deploymentId, force = false)
            ?: throw IllegalArgumentException("DeploymentId not created or not found")
    }


    suspend fun getKeyConfig(byDeploymentId: UUID? = null): Model {
        val deploymentId = getDeploymentId()
        val keyConfig = useBackend.getConfig(byDeploymentId = deploymentId, keyId = keyId)
            ?: throw IllegalArgumentException("Key with deploymentId: $byDeploymentId, keyId: $keyId not found")
        return keyConfig
    }
}