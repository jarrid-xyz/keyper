package jarrid.keyper.resource

import kotlinx.serialization.Contextual
import java.util.*

class ResourceIsUndefinedException(message: String = "Resource must be defined in the payload") :
    Exception(message)

data class BasePayload(
    val id: UUID? = null,
    val name: String? = null,
    val context: Map<String, Any>? = null,
) {
    fun getConfigAttribute(attr: String, default: Any?): Any? {
        return this.context?.getOrDefault(attr, default) ?: default
    }
}

data class Payload(
    val deployment: BasePayload? = null,
    val context: Map<String, @Contextual Any>? = null,
    val resource: BasePayload? = null,
)