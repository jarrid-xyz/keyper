package jarrid.keyper.tfcdk

import com.github.f4b6a3.uuid.codec.base.Base62Codec
import com.hashicorp.cdktf.TerraformStack
import jarrid.keyper.app.Config
import jarrid.keyper.key.Model
import software.constructs.Construct
import java.util.*

abstract class KeyStack(
    scope: Construct,
    terraformId: UUID,
) : TerraformStack(scope, terraformId.toString()) {
    companion object {
        val appConfig = Config().get()

        fun getKeyConfigOptions(config: Model, option: String): String? {
            val keyConfigOptions: Map<String, Any>? = config.context?.get("options") as? Map<String, Any>
            return keyConfigOptions?.get(option) as? String
        }

        fun getSanitizedName(uuid: UUID): String {
            return "key-${base62Encode(uuid)}"
        }

        private fun base62Encode(uuid: UUID): String {
            val encoder = Base62Codec()
            val encoded = encoder.encode(uuid)
            return encoded
        }
    }

    init {
        useProvider()
    }

    abstract fun convert(configs: List<Model>): StackTfvars
    abstract fun useProvider()
    abstract suspend fun create(tfvar: StackTfvars): Any
}