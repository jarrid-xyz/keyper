package jarrid.keyper.utils.file

import jarrid.keyper.app.Config
import jarrid.keyper.key.Model
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*

abstract class Backend {
    private val rootDirPath: String = System.getProperty("projectRoot")
    val rootDir: Path = Paths.get(rootDirPath)

    val dir: String
        get() = appConfig.get().manager.file.path
    private val appConfig: Config
        get() = Config()

    fun getPrefix(keyConfig: Model): String {
        return joinPaths(dir, keyConfig.deploymentId.toString())
    }

    fun getFileName(keyConfig: Model): String {
        return joinPaths(getPrefix(keyConfig), "${keyConfig.keyId!!}.json")
    }

    companion object {
        fun joinPaths(vararg paths: String): String {
            return Paths.get("", *paths).toString()
        }
    }

    abstract suspend fun write(keyConfig: Model)
    abstract suspend fun getOrCreateDeploymentId(byDeploymentId: UUID?, force: Boolean = true): UUID?
    abstract suspend fun getDeploymentIds(): List<UUID>
    abstract suspend fun getConfigs(): List<Model>
}