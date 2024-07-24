package jarrid.keyper.resource.key

import jarrid.keyper.resource.Base
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Resource
import jarrid.keyper.resource.ResourceType
import jarrid.keyper.utils.model.NewUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*


@Serializable
data class Model(
    @Transient private val id: UUID = NewUUID.getEmpty(),
    @Transient val ttl: Int = 7,
    @Transient val rotationPeriod: String = "7776000s", // 90 days
    @Transient private val name: String? = null,
    @Transient private val context: Map<String, Any>? = null,
) : Resource(
    base = Base(id = id, name = name, context = context),
    type = ResourceType.KEY,
) {
    companion object {
        fun create(payload: BasePayload): Model {
            val out = Model(
                id = payload.id ?: NewUUID.get(),
                name = payload.name,
                ttl = payload.getConfigAttribute("ttl", 7) as Int,
                rotationPeriod = payload.getConfigAttribute("rotation_period", "7776000s") as String
            )
            out.base.create()
            return out
        }
    }
}