package jarrid.keyper.resource.key

import jarrid.keyper.resource.Base
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Resource
import jarrid.keyper.resource.ResourceType
import jarrid.keyper.utils.model.NewUUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*


@Serializable
data class Model(
    val ttl: Int = 7,
    @Contextual val id: UUID,
    val name: String? = null,
    val context: Map<String, @Contextual Any>? = null,
) : Resource(
    base = Base(id = id, name = name, context = context),
    type = ResourceType.KEY,
) {
    companion object {
        fun create(payload: BasePayload): Model {
            val out = Model(
                id = payload.id ?: NewUUID.get(),
                name = payload.name,
                ttl = payload.getConfigAttribute("ttl", 7) as Int
            )
            out.base.create()
            return out
        }
    }
}