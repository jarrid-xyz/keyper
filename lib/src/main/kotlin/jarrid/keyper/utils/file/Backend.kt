package jarrid.keyper.utils.file

import jarrid.keyper.resource.key.Model
import jarrid.keyper.tfcdk.DeploymentStack
import java.nio.file.Paths
import java.util.*

class KeyConfigNotFound(deploymentId: UUID, keyId: UUID) :
    Exception("Key config with deploymentId: $deploymentId and keyId: $keyId not found")

interface Backend {
    companion object {
        fun joinPaths(vararg paths: String): String {
            return Paths.get("", *paths).toString()
        }
    }

    suspend fun write(keyConfig: Model)
    suspend fun getOrCreateDeploymentId(byDeploymentId: UUID?, force: Boolean = true): UUID?
    suspend fun getDeploymentIds(): List<UUID>
    suspend fun getConfig(deploymentId: UUID, keyId: UUID): Model
    suspend fun getDeploymentStacks(): List<DeploymentStack>
}