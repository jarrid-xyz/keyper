package jarrid.keyper.utils.file

import io.klogging.Klogging
import jarrid.keyper.resource.*
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.utils.json.SerDe
import jarrid.keyper.utils.model.InvalidUUIDException
import jarrid.keyper.utils.model.isEmpty
import jarrid.keyper.utils.model.toUUID
import java.nio.file.Paths
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

class DeploymentNotFoundException(message: String) : Exception(message)
class MultipleDeploymentsFoundException(message: String) : Exception(message)
class ResourceNotFoundException(message: String) : Exception(message)
class DirectoryNotFoundException(message: String) : Exception(message)


abstract class Backend(config: Config) : Klogging {
    private val app = config.get()
    val root: String = app.outDir
    private val serde = SerDe()
    private val dir: String = app.manager.file.path

    companion object {
        fun joinPaths(vararg paths: String): String {
            return Paths.get("", *paths).toString()
        }

        fun addFileExt(name: String, ext: String = "json"): String {
            return "$name.$ext"
        }

        fun removeFileExt(name: String, ext: String = "json"): String {
            return name.removeSuffix(".$ext")
        }
    }

    private fun getPath(deployment: Deployment): String {
        return joinPaths(root, dir, deployment.id.toString())
    }

    private fun getFileName(resource: Resource, deployment: Deployment): String {
        return joinPaths(
            root,
            dir,
            deployment.id.toString(),
            resource.type.toString().lowercase(),
            addFileExt(resource.base.id.toString())
        )
    }

    private fun getFileName(deployment: Deployment): String {
        return joinPaths(root, dir, deployment.id.toString(), addFileExt("deployment"))
    }

    suspend fun createDeploymentDir(deployment: Deployment? = null) {
        val useDeployment = deployment ?: Deployment.create(BasePayload())
        val path = getPath(useDeployment)
        if (!exists(path)) {
            createDir(path)
            logger.info("Created directory for deployment: $path")
        } else {
            logger.info("Skipped: directory exists for deployment: $path")
        }
    }

    suspend fun write(deployment: Deployment) {
        val encoded = serde.encode(deployment)
        val filePath = getFileName(deployment)
        write(filePath, encoded)
        logger.info("Write to file: $filePath")
    }

    suspend fun write(resource: Resource, deployment: Deployment) {
        val encoded = serde.encode(resource)
        val filePath = getFileName(resource, deployment)
        write(filePath, encoded)
        logger.info("Write to file: $filePath")
    }

    abstract fun write(path: String, encoded: String)
    abstract fun exists(path: String): Boolean
    abstract fun createDir(path: String)
    abstract fun ls(dir: String): List<String>
    abstract fun read(path: String): String

    fun getDeployment(deployment: Deployment? = null): Deployment {
        var deployments = getDeployments()
        if (deployment != null) {
            deployments = deployments.filter {
                val idMatch = deployment.id.isEmpty() || it.id == deployment.id
                val nameMatch = deployment.name.isEmpty() || it.name == deployment.name
                idMatch && nameMatch
            }
        }

        val out: Deployment = when {
            deployments.isEmpty() -> throw DeploymentNotFoundException("Deployment not found, run keyper deploy init to create deployment")
            deployments.size > 1 -> throw MultipleDeploymentsFoundException("Multiple deployments found, please specify a deployment.")
            else -> deployments.first()
        }
        return out
    }

    fun getDeployments(): List<Deployment> {
        val out: MutableList<Deployment> = mutableListOf()
        val folders = ls(dir)
        for (folder in folders) {
            val deploymentFileName = getFileName(Deployment(_id = folder.toUUID()))
            val deployment: Deployment = serde.decode(read(deploymentFileName))
            out.add(deployment)
        }
        return out
    }

    fun getResource(resource: Resource): Resource {
        val deployment = getDeployment()
        val fileName = getFileName(resource, deployment)
        return serde.decode(read(fileName))
    }

    suspend fun getResources(deployment: Deployment, type: ResourceType): List<Resource> {
        val useDeployment = getDeployment(deployment)
        val files = ls(joinPaths(getPath(useDeployment), type.toString().lowercase()))
        val out: MutableList<Resource> = mutableListOf()
        for (file in files) {
            val fileName = removeFileExt(file)
            try {
                val resourceFileName =
                    getFileName(Resource(base = Base(id = fileName.toUUID()), type = type), useDeployment)
                val resource: Resource = serde.decode(read(resourceFileName))
                out.add(resource)
            } catch (e: InvalidUUIDException) {
                logger.warn("File name $fileName cannot be cast to UUID, skipping.")
            }
        }
        return out
    }

    suspend fun getDeploymentStack(): List<DeploymentStack> {
        val deployments = getDeployments()
        val out: MutableList<DeploymentStack> = mutableListOf()
        for (deployment in deployments) {
            val keys = getResources(deployment, ResourceType.KEY) as List<Key>
            val roles = getResources(deployment, ResourceType.ROLE) as List<Role>
            out.add(
                DeploymentStack(
                    deployment = deployment,
                    keys = keys,
                    roles = roles
                )
            )
        }
        return out
    }
}