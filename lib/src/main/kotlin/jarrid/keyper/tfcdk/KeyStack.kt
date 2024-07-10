package jarrid.keyper.tfcdk

import com.github.f4b6a3.uuid.codec.base.Base62Codec
import com.hashicorp.cdktf.TerraformStack
import jarrid.keyper.app.Config
import jarrid.keyper.app.Stack
import jarrid.keyper.key.DeploymentStack
import jarrid.keyper.key.Model
import jarrid.keyper.tfcdk.gcp.stack.Tfvars
import kotlinx.coroutines.runBlocking
import software.constructs.Construct
import java.util.*

abstract class KeyStack(
    scope: Construct,
    stackName: String,
) : TerraformStack(scope, stackName) {
    companion object {
        val stack: Stack = Stack.GCP
        private val appConfig = Config().get()
        val provider = stack.getConfig(appConfig)!!

        fun getKeyConfigOptions(config: Model, option: String): String? {
            val keyConfigOptions: Map<String, Any>? = config.context?.get("options") as? Map<String, Any>
            return keyConfigOptions?.get(option) as? String
        }

        private fun base62Encode(uuid: UUID): String {
            // shorter uuid encoding option
            val encoder = Base62Codec()
            val encoded = encoder.encode(uuid)
            return encoded
        }
    }

    init {
        runBlocking {
            useProvider()
        }
    }

    abstract fun convert(configs: List<DeploymentStack>): Tfvars
    abstract suspend fun useProvider()
    abstract suspend fun create(tfvar: StackTfvars): Any
}