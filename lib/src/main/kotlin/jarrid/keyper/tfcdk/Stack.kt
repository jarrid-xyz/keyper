package jarrid.keyper.tfcdk

import com.github.f4b6a3.uuid.codec.base.Base62Codec
import com.hashicorp.cdktf.LocalBackend
import com.hashicorp.cdktf.LocalBackendConfig
import com.hashicorp.cdktf.TerraformStack
import io.klogging.Klogging
import jarrid.keyper.app.CloudProviderConfig
import jarrid.keyper.app.Config
import jarrid.keyper.app.TfBackendType
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.iam.MultipleRolesFoundException
import jarrid.keyper.resource.iam.RoleNameIsUndefinedException
import jarrid.keyper.resource.iam.RoleNotFoundException
import kotlinx.coroutines.runBlocking
import software.constructs.Construct
import java.util.*
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

abstract class Stack(
    scope: Construct,
    val stackName: String = "default",
) : Klogging, TerraformStack(scope, stackName) {

    abstract val provider: CloudProviderConfig
    val config = Config().get()
    val stack = config.provider.tfcdk.stack

    companion object {
        private fun base62Encode(uuid: UUID): String {
            // shorter uuid encoding option
            val encoder = Base62Codec()
            val encoded = encoder.encode(uuid)
            return encoded
        }

        fun getRole(name: String, tfvar: DeploymentStack): Role {
            val filtered = tfvar.roles.filter { role -> role.base.name == name }
            return when {
                filtered.isEmpty() -> throw RoleNotFoundException(name)
                filtered.size > 1 -> throw MultipleRolesFoundException(name)
                else -> filtered.first()
            }
        }
    }

    init {
        runBlocking {
            useBackend()
            useProvider()
        }
    }

    private fun useLocalBackend() {
        LocalBackend(
            this, LocalBackendConfig.builder()
                .path("terraform.tfstate")
                .build()
        )
    }

    suspend fun useBackend() {
        logger.info("Backend is set to ${provider.backend}")
        when (provider.backend.type) {
            TfBackendType.LOCAL -> useLocalBackend()
            TfBackendType.CLOUD -> useCloudBackend()
        }
    }

    fun getLabels(key: Key, deployment: Deployment): Map<String, String> {
        return mapOf(
            "stack-name" to stackName,
            "key-id" to key.base.id.toString(),
            "deployment-id" to deployment.base.id.toString()
        )
    }

    fun validateRole(role: Role, tfvar: DeploymentStack): String {
        val name = role.base.name ?: throw RoleNameIsUndefinedException()
        getRole(name, tfvar)
        return name
    }

    abstract suspend fun useProvider()
    abstract suspend fun useCloudBackend()
    abstract fun createKeys(tfvar: DeploymentStack): CreateKeysOutput
    abstract fun createRoles(tfvar: DeploymentStack): CreateRolesOutput
    abstract fun createPermissions(
        tfvar: DeploymentStack,
        createKeys: CreateKeysOutput,
        createRoles: CreateRolesOutput
    ): CreatePermissionsOutput

    suspend fun create(tfvar: DeploymentStack) {
        val createKeys = createKeys(tfvar)
        val createRoles = createRoles(tfvar)
        createPermissions(tfvar, createKeys, createRoles)
        logger.info("Created terraform stack")
    }
}