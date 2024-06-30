package jarrid.keyper.utils.file

import jarrid.keyper.app.Config
import jarrid.keyper.key.Model
import java.nio.file.Paths
import java.util.*

abstract class Backend {
    val rootDir = System.getProperty("projectRoot")

    val dir: String
        get() = appConfig.get().manager.file.path
    private val appConfig: Config
        get() = Config()

    fun getPrefix(keyConfig: Model): String {
        return joinPaths(dir, keyConfig.deploymentId.toString())
    }

    fun getFilename(keyConfig: Model): String {
        return joinPaths(getPrefix(keyConfig), "${keyConfig.keyId!!}.json")
    }

    companion object {
        fun joinPaths(vararg paths: String): String {
            return Paths.get("", *paths).toString()
        }
    }

    abstract suspend fun write(keyConfig: Model)
    abstract suspend fun getOrCreateDeploymentId(byDeploymentId: UUID?): UUID
    abstract suspend fun getDeploymentIds(): List<UUID>
    abstract suspend fun getConfigs(): List<Model>
    abstract suspend fun getConfigs(deploymentId: UUID): List<Model>
}