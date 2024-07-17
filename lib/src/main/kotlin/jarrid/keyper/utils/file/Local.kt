package jarrid.keyper.utils.file

import io.klogging.Klogging
import jarrid.keyper.resource.Config
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.resource.key.Model
import jarrid.keyper.utils.json.decode
import jarrid.keyper.utils.json.encode
import jarrid.keyper.utils.model.NewUUID
import jarrid.keyper.utils.model.stripFileTypeAndToUUID
import java.nio.file.Files
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.util.*


class Local(config: Config) : Backend, Klogging {
    private val appConfig = config.get()
    val rootDir: Path = config.outDir
    private val dir: String = appConfig.manager.file.path

    fun getPrefix(keyConfig: Model): String {
        return Backend.joinPaths(dir, keyConfig.deploymentId.toString())
    }

    fun getFileName(keyConfig: Model): String {
        return Backend.joinPaths(getPrefix(keyConfig), "${keyConfig.keyId!!}.json")
    }

    private fun ls(dir: String): List<String> {
        val dirPath = rootDir.resolve(dir)
        if (!Files.isDirectory(dirPath)) {
            throw IllegalArgumentException("Dir: $dir doesn't exist or is not a directory. Root dir: $rootDir")
        }
        return Files.list(dirPath).map { it.fileName.toString() }.toList()
    }

    suspend fun createDirs(path: String) {

    }
    suspend fun createDir(keyConfig: Model) {
        val prefix = getPrefix(keyConfig)
        if (Files.exists(rootDir.resolve(prefix))) {
            logger.info("Skipped: dir exists for prefix: $prefix")
            return
        }
        Files.createDirectories(rootDir.resolve(prefix))
        logger.info("Created dir for prefix: $prefix")
    }

    suspend fun writeFile(keyConfig: Model) {
        val encoded = encode(keyConfig)
        val fileName = getFileName(keyConfig)
        Files.writeString(rootDir.resolve(fileName), encoded)
        logger.info("Write to file: $fileName")
    }

    override suspend fun write(keyConfig: Model) {
        createDir(keyConfig)
        writeFile(keyConfig)
    }

    override suspend fun getDeploymentIds(): List<UUID> {
        try {
            return ls(dir).map { UUID.fromString(it) }
        } catch (e: IllegalArgumentException) {
            logger.warn("Config dir: $dir doesn't exist at $rootDir, create new deploymentId")
        }
        return emptyList()
    }

    override suspend fun getConfig(deploymentId: UUID, keyId: UUID): Model {
        val config: Model
        val path = Backend.joinPaths(dir, deploymentId.toString(), keyId.toString())
        val filePath = rootDir.resolve("$path.json")
        try {
            val string = Files.readString(filePath)
            config = decode(string)

        } catch (e: NoSuchFileException) {
            throw KeyConfigNotFound(deploymentId, keyId)
        }
        return config
    }

    private suspend fun getConfigs(deploymentId: UUID): List<Model> {
        val out: MutableList<Model> = mutableListOf()
        val configIds = ls(Backend.joinPaths(dir, deploymentId.toString()))
        for (configId in configIds) {
            val config = getConfig(deploymentId, configId.stripFileTypeAndToUUID(".json"))
            out.add(config)
        }
        return out
    }

    override suspend fun getDeploymentStacks(): List<DeploymentStack> {
        val out: MutableList<DeploymentStack> = mutableListOf()
        val deploymentIds = getDeploymentIds()
        for (deploymentId in deploymentIds) {
            val configs = getConfigs(deploymentId)
            out.add(
                DeploymentStack(
                    deploymentId = deploymentId,
                    keys = configs,
                    serviceAccounts = emptyList()
                )
            )
        }
        return out
    }

    override suspend fun getOrCreateDeploymentId(byDeploymentId: UUID?, force: Boolean): UUID? {
        val deploymentIds = getDeploymentIds()
        var deploymentId: UUID? = null
        when (deploymentIds.size) {
            0 -> {
                logger.info("Existing deploymentId not found.")
                if (force) {
                    deploymentId = NewUUID.get()
                    logger.info("Create new deploymentId: $deploymentId")
                }
            }

            1 -> {
                deploymentId = deploymentIds[0]
                logger.info("Found existing deploymentId: $deploymentId")
            }

            else -> {
                if (byDeploymentId !in deploymentIds) {
                    logger.error("Invalid: deploymentId: $byDeploymentId doesn't exist.")
                    if (force) {
                        deploymentId = NewUUID.get()
                        logger.info("Create new deploymentId: $deploymentId")
                    }
                }
            }
        }
        return deploymentId
    }
}