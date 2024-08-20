package jarrid.keyper.tfcdk

import com.hashicorp.cdktf.App
import com.hashicorp.cdktf.AppConfig
import com.hashicorp.cdktf.providers.aws.iam_role.IamRole
import com.hashicorp.cdktf.providers.aws.kms_key.KmsKey
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKey
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRing
import com.hashicorp.cdktf.providers.google.service_account.ServiceAccount
import io.klogging.Klogging
import jarrid.keyper.resource.Deployment
import jarrid.keyper.tfcdk.Stack
import jarrid.keyper.tfcdk.stack.aws.IamPolicyOutput
import jarrid.keyper.tfcdk.stack.gcp.CreateIamPolicyOutput
import jarrid.keyper.utils.file.Backend
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import com.hashicorp.cdktf.App as CdktfApp
import jarrid.keyper.app.App as ResourceConfig
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key
import jarrid.keyper.tfcdk.Stack as TfStack

sealed class CreatePermissionsOutput

data class GcpCreatePermissionsOutput(
    val policies: List<CreateIamPolicyOutput>,
) : CreatePermissionsOutput()

data class AwsCreatePermissionsOutput(
    val out: IamPolicyOutput
) : CreatePermissionsOutput()

sealed class CreateKeysOutput

data class AwsCreateKeysOutput(
    val keys: Map<Key, KmsKey>
) : CreateKeysOutput()

data class GcpCreateKeysOutput(
    val keyRing: KmsKeyRing,
    val keys: Map<Key, KmsCryptoKey>
) : CreateKeysOutput()

sealed class CreateRolesOutput

data class GcpCreateRolesOutput(
    val roles: Map<Role, ServiceAccount>
) : CreateRolesOutput()

data class AwsCreateRolesOutput(
    val roles: Map<Role, IamRole>
) : CreateRolesOutput()


@Serializable
data class DeploymentStack(
    @Contextual val deployment: Deployment,
    @Contextual val keys: List<Key>,
    @Contextual val roles: List<Role>,
)

class TfApp(val config: ResourceConfig) : Klogging {

    suspend fun getAppConfig(): AppConfig {
        val root: String = config.outDir
        val path: String = config.provider.tfcdk.path
        logger.info("Current root: $root, tfcdk path: $path")
        return AppConfig.builder()
            .outdir(Backend.joinPaths(root, path))
            .build()
    }

    suspend fun getTfApp(): App {
        return CdktfApp(getAppConfig())
    }

    fun getTfStack(stack: KClass<out TfStack>, app: App, name: String): Stack {
        val stackConstructor =
            requireNotNull(stack.primaryConstructor) { "Primary constructor not found for stack class: ${stack.qualifiedName}" }
        return stackConstructor.call(app, name)
    }

    suspend fun create(): App {
        val backend: Backend = config.resource.backend.backend.get()
        val stack: KClass<out TfStack> = config.provider.tfcdk.stack.get()
        val deployments = backend.getDeploymentStack()
        val app = getTfApp()

        deployments.forEach { deployment ->
            val tfStack = getTfStack(stack, app, deployment.deployment.name)
            tfStack.create(deployment)
            logger.info("Create terraform stack for deployment: ${deployment.deployment.name}")
        }

        logger.info("Run terraform synth for deployments: ${deployments.map { it.deployment.name }}")
        app.synth()
        logger.info("Finished terraform synth.")
        return app
    }

    suspend fun run() {
        create()
    }
}