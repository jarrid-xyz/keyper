package jarrid.keyper.utils.file

import io.klogging.Klogging
import jarrid.keyper.key.Model
import jarrid.keyper.utils.json.decode
import jarrid.keyper.utils.json.encode
import jarrid.keyper.utils.model.NewUUID
import java.io.File
import java.util.*

class Local : Backend(), Klogging {
    private fun ls(dir: String): List<String> {
        val dirFile = File(rootDir, dir)
        if (!dirFile.isDirectory) {
            throw IllegalArgumentException("Dir: $dir doesn't exist or is not a directory. Root dir: $rootDir")
        }
        return dirFile.list()?.toList() ?: emptyList()
    }

    private fun getConfig(path: String): Model {
        val file = File(rootDir, path)
        val string = file.readText()
        return decode(string)
    }


    private suspend fun createDir(keyConfig: Model) {
        val prefix = getPrefix(keyConfig)
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
        val fileName = getFilename(keyConfig)
        val file = File(rootDir, fileName)
        file.writeText(encoded)
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
            logger.error(e, "Config dir: $dir doesn't exist, create new deploymentId")
        }
        return emptyList()
    }

    override suspend fun getConfigs(): List<Model> {
        val deploymentIds = getDeploymentIds()
        val pairs: MutableList<Pair<UUID, String>> = mutableListOf()
        for (deploymentId in deploymentIds) {
            val configIds = ls(joinPaths(dir, deploymentId.toString()))
            for (configId in configIds) {
                pairs.add(Pair(deploymentId, configId))
            }
        }
        val out: MutableList<Model> = mutableListOf()
        for (pair in pairs) {
            val deploymentId: UUID = pair.first
            val configId: String = pair.second
            val config = getConfig(joinPaths(dir, deploymentId.toString(), configId))
            out.add(config)
        }
        return out
    }

    override suspend fun getConfigs(deploymentId: UUID): List<Model> {
        TODO("Not yet implemented")
    }

    override suspend fun getOrCreateDeploymentId(byDeploymentId: UUID?): UUID {
        val deploymentIds = getDeploymentIds()
        var deploymentId: UUID = NewUUID.get()
        when (deploymentIds.size) {
            0 -> {
                return deploymentId
            }

            1 -> {
                deploymentId = deploymentIds[0]
                logger.info("Found existing deploymentId: $deploymentId")
                return deploymentId
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