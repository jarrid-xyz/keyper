package jarrid.keyper.resource

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
open class Model(
    @Contextual val resource: Resource,
    @Contextual val deployment: Deployment,
) {
    companion object {
        fun get(resource: Resource, deployment: Deployment): Model {
            return Model(
                resource = resource,
                deployment = deployment
            )
        }
    }
}

@Serializable
data class BasePayload(
    @Contextual val deployment: Deployment,
    val name: String? = null,
    val context: Map<String, @Contextual Any>? = null
)

data class Payload(
    val base: BasePayload,
    val resource: Resource? = null,
    val config: Map<String, Any>? = null,
) {
    fun getConfigAttribute(attr: String): Any? {
        return config?.get(attr)
    }
}
