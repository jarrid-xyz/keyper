package jarrid.keyper.resource.iam

import jarrid.keyper.resource.Base
import jarrid.keyper.resource.ResourceType
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

class RoleIsUndefinedException(message: String = "Role name must be defined in the payload") :
    Exception(message = message)


@Serializable
data class Model(
    @Contextual val id: UUID,
    val name: String,
    val context: Map<String, @Contextual Any>? = null,
) : jarrid.keyper.resource.Resource(
    base = Base(id = id, name = name, context = context),
    type = ResourceType.ROLE,
)