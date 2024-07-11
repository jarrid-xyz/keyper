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
    private val stack: KClass<out KeyStack>,
) : Klogging {
    private val appConfig = Config().get()
    private val root: String = appConfig.outDir

    private suspend fun getAppConfig(): AppConfig {
        logger.info("Current root: $root")
        return AppConfig.builder()
            .outdir(Backend.joinPaths(root, "cdktf.out"))
            .build()
    }

    private suspend fun create(): App {
        val deployments = backend.getDeploymentStacks()
        val app = App(getAppConfig())
        val constructor = stack.primaryConstructor
            ?: throw IllegalArgumentException("KeyStack class must have a primary constructor")
        val keyStack = constructor.call(app)

        val tfvars = keyStack.convert(deployments)
        keyStack.create(tfvars)

        logger.info("Run terraform synth for deployments: $deployments")
        app.synth()
        logger.info("Finished terraform synth.")
        return app
    }

    suspend fun run() {
        create()
    }
}