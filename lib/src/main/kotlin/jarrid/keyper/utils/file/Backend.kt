package jarrid.keyper.utils.file

import jarrid.keyper.key.Model
import java.nio.file.Paths
import java.util.*

interface Backend {
    companion object {
        fun joinPaths(vararg paths: String): String {
            return Paths.get("", *paths).toString()
        }
    }

    suspend fun write(keyConfig: Model)
    suspend fun getOrCreateDeploymentId(byDeploymentId: UUID?, force: Boolean = true): UUID?
    suspend fun getDeploymentIds(): List<UUID>
    suspend fun getConfig(byDeploymentId: UUID? = null, keyId: UUID): Model?
    suspend fun getConfigs(): List<Model>
}