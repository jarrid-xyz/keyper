package jarrid.keyper.utils.file

import io.klogging.Klogging
import jarrid.keyper.app.Config
import jarrid.keyper.key.Model
import jarrid.keyper.utils.json.decode
import jarrid.keyper.utils.json.encode
import jarrid.keyper.utils.model.NewUUID
import java.nio.file.Files
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

    private fun getConfig(path: String): Model {
        val filePath = rootDir.resolve(path)
        val string = Files.readString(filePath)
        return decode(string)
    }


    override suspend fun getConfig(byDeploymentId: UUID?, keyId: UUID): Model? {
        val configs = getConfigs()
        for (config in configs) {
            if (keyId == config.keyId) {
                if (byDeploymentId != null) {
                    if (byDeploymentId == config.deploymentId) {
                        return config
                    }
                } else {
                    return config
                }
            }
        }
        logger.info("Key with deploymentId: $byDeploymentId, keyId: $keyId not found")
        return null
    }

    override suspend fun getConfigs(): List<Model> {
        val deploymentIds = getDeploymentIds()
        val pairs: MutableList<Pair<UUID, String>> = mutableListOf()
        for (deploymentId in deploymentIds) {
            val configIds = ls(Backend.joinPaths(dir, deploymentId.toString()))
            for (configId in configIds) {
                pairs.add(Pair(deploymentId, configId))
            }
        }
        val out: MutableList<Model> = mutableListOf()
        for (pair in pairs) {
            val deploymentId: UUID = pair.first
            val configId: String = pair.second
            val config = getConfig(Backend.joinPaths(dir, deploymentId.toString(), configId))
            out.add(config)
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