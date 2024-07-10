package jarrid.keyper.tfcdk

import com.hashicorp.cdktf.App
import com.hashicorp.cdktf.AppConfig
import io.klogging.Klogging
import jarrid.keyper.app.Config
import jarrid.keyper.utils.file.Backend
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor

class Stack(
    private val backend: Backend,
    private val stack: KClass<out KeyStack>
) : Klogging {
    private val appConfig = Config().get()
    private val root: String = appConfig.outDir

    private suspend fun getAppConfig(): AppConfig {
        logger.info("Current root: $root")
        return AppConfig.builder()
            .outdir(Backend.joinPaths(root, "cdktf.out"))
            .build()
    }

    private suspend fun create(): List<App> {
        val deployments = backend.getDeploymentStacks()
        val apps: MutableList<App> = mutableListOf()
        for (deployment in deployments) {
            logger.info("Create terraform stack for deploymentId: $deployment.deploymentId")
            val app = App(getAppConfig())
            val constructor = stack.primaryConstructor
                ?: throw IllegalArgumentException("KeyStack class must have a primary constructor")
            val keyStack = constructor.call(app, deployment.deploymentId)
            val tfvar = keyStack.convert(deployment.keys)
            keyStack.create(tfvar)
            logger.info("Run terraform synth for deploymentId: ${deployment.deploymentId}")
            app.synth()
            logger.info("Finished terraform synth for deploymentId: ${deployment.deploymentId}")
            app
        }
        return apps
    }

    suspend fun run() {
        create()
    }
}