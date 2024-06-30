package jarrid.keyper.utils.file

import jarrid.keyper.app.Config
import jarrid.keyper.key.Model
import java.nio.file.Paths
import java.util.*

interface Backend {
    companion object {
        private val appConfig = Config()
        private val dir: String = appConfig.get().manager.file.path

        fun joinPaths(vararg paths: String): String {
            return Paths.get("", *paths).toString()
        }

        fun getConfigsRoot(): String {
            return dir
        }

        fun getPrefix(keyConfig: Model): String {
            return joinPaths(dir, keyConfig.deploymentId.toString())
        }

        fun getFilename(keyConfig: Model): String {
            return joinPaths(getPrefix(keyConfig), "${keyConfig.keyId!!}.json")
        }

    }

    suspend fun write(keyConfig: Model)
    suspend fun getOrCreateDeploymentId(byDeploymentId: UUID?): UUID
    suspend fun getDeploymentIds(): List<UUID>
    suspend fun getConfigs(): List<Model>
    suspend fun getConfigs(deploymentId: UUID): List<Model>
}