package jarrid.keyper.resource.key

import jarrid.keyper.resource.Base
import jarrid.keyper.resource.Resource
import jarrid.keyper.resource.ResourceType
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
)