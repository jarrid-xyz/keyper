package jarrid.keyper.utils.file

import io.klogging.Klogging
import jarrid.keyper.key.Model
import jarrid.keyper.utils.json.decode
import jarrid.keyper.utils.json.encode
import java.io.File
import java.util.*

class Local : Backend, Klogging {
    private val rootDir = System.getProperty("projectRoot")
    private fun ls(dir: String): List<String> {
        val dirFile = File(rootDir, dir)
        if (!dirFile.isDirectory) {
            throw RuntimeException("Dir: $dir doesn't exist or is not a directory. Root dir: $rootDir")
        }
        return dirFile.list()?.toList() ?: emptyList()
    }

    private fun getConfig(path: String): Model {
        val file = File(rootDir, path)
        val string = file.readText()
        return decode(string)
    }


    private suspend fun createDir(keyConfig: Model) {
        val prefix = Backend.getPrefix(keyConfig)
        val dir = File(rootDir, prefix)
        if (dir.exists()) {
            logger.info("Skipped: dir exists for prefix: $prefix")
            return
        }
        dir.mkdirs()
        logger.info("Created dir for prefix: $prefix")
    }

    private suspend fun writeFile(keyConfig: Model) {
        val encoded = encode(keyConfig)
        val fileName = Backend.getFilename(keyConfig)
        val file = File(rootDir, fileName)
        file.writeText(encoded)
        logger.info("Write to file: $fileName")
    }

    override suspend fun write(keyConfig: Model) {
        createDir(keyConfig)
        writeFile(keyConfig)
    }

    override suspend fun getDeploymentIds(): List<UUID> {
        val root = Backend.getConfigsRoot()
        return ls(root).map { UUID.fromString(it) }
    }

    override suspend fun getConfigs(): List<Model> {
        val root = Backend.getConfigsRoot()
        val deploymentIds = getDeploymentIds()
        val pairs: MutableList<Pair<UUID, String>> = mutableListOf()
        for (deploymentId in deploymentIds) {
            val configIds = ls(Backend.joinPaths(root, deploymentId.toString()))
            for (configId in configIds) {
                pairs.add(Pair(deploymentId, configId))
            }
        }
        val out: MutableList<Model> = mutableListOf()
        for (pair in pairs) {
            val deploymentId: UUID = pair.first
            val configId: String = pair.second
            val config = getConfig(Backend.joinPaths(root, deploymentId.toString(), configId))
            out.add(config)
        }
        return out
    }

    override suspend fun getConfigs(deploymentId: UUID): List<Model> {
        TODO("Not yet implemented")
    }

    override suspend fun getOrCreateDeploymentId(byDeploymentId: UUID?): UUID {
        val deploymentIds = getDeploymentIds()
        var deploymentId: UUID = UUID.randomUUID()
        when (deploymentIds.size) {
            0 -> {}
            1 -> {
                deploymentId = deploymentIds[0]
                logger.info("Found existing deploymentId: $deploymentId")
            }

            else -> {
                if (byDeploymentId in deploymentIds) {
                    logger.error("Invalid: deploymentId: $byDeploymentId doesn't exist. Create new deploymentId: $deploymentId")
                } else {
                    deploymentId = byDeploymentId!!
                }
            }
        }
        return deploymentId
    }
}