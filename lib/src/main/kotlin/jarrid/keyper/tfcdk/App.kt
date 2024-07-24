package jarrid.keyper.tfcdk

import com.hashicorp.cdktf.App
import com.hashicorp.cdktf.AppConfig
import io.klogging.Klogging
import jarrid.keyper.resource.Config
import jarrid.keyper.resource.Deployment
import jarrid.keyper.utils.file.Backend
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import com.hashicorp.cdktf.App as CdktfApp
import jarrid.keyper.resource.key.Model as Key
import jarrid.keyper.tfcdk.Stack as TfStack

@Serializable
data class DeploymentStack(
    @Contextual val deployment: Deployment,
    @Contextual val keys: List<Key>,
    @Contextual val roles: List<jarrid.keyper.resource.iam.Model>,
)

class App : Klogging {
    private val config = Config().get()
    private val root: String = config.outDir
    private val backend: Backend = config.resource.backend.backend.get()
    private val stack: KClass<out TfStack> = config.provider.tfcdk.stack.get()
    private val path: String = config.provider.tfcdk.path

    private suspend fun getAppConfig(): AppConfig {
        logger.info("Current root: $root, tfcdk path: $path")
        return AppConfig.builder()
            .outdir(Backend.joinPaths(root, path))
            .build()
    }

    private suspend fun create(): App {
        val deployments = backend.getDeploymentStack()
        val app = CdktfApp(getAppConfig())
        val stackConstructor =
            requireNotNull(stack.primaryConstructor) { "Primary constructor not found for stack class: ${stack.qualifiedName}" }

        deployments.forEach { deployment ->
            val tfStack = stackConstructor.call(app, deployment.deployment.name)
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