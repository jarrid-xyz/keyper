package jarrid.keyper.tfcdk

import com.github.f4b6a3.uuid.codec.base.Base62Codec
import com.hashicorp.cdktf.TerraformStack
import jarrid.keyper.app.Config
import kotlinx.coroutines.runBlocking
import software.constructs.Construct
import java.util.*

abstract class Stack(
    scope: Construct,
    val stackName: String = "default",
) : TerraformStack(scope, stackName) {

    val config = Config().get()
    val stack = config.provider.tfcdk.stack

    companion object {
        private fun base62Encode(uuid: UUID): String {
            // shorter uuid encoding option
            val encoder = Base62Codec()
            val encoded = encoder.encode(uuid)
            return encoded
        }
    }

    init {
        runBlocking {
            useBackend()
            useProvider()
        }
    }

    abstract suspend fun useBackend()
    abstract suspend fun useProvider()
    abstract suspend fun create(tfvar: DeploymentStack)
}