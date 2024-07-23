package jarrid.keyper.resource.iam

import jarrid.keyper.resource.Base
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Resource
import jarrid.keyper.resource.ResourceType
import jarrid.keyper.utils.model.NewUUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

class RoleNameIsUndefinedException(message: String = "Role name must be defined in the payload") :
    Exception(message)


@Serializable
data class Model(
    @Contextual val id: UUID,
    val name: String,
    val context: Map<String, @Contextual Any>? = null,
) : Resource(
    base = Base(id = id, name = name, context = context),
    type = ResourceType.ROLE,
) {
    companion object {
        fun create(payload: BasePayload): Model {
            val name = payload.name ?: throw RoleNameIsUndefinedException()

            val out = Model(
                id = payload.id ?: NewUUID.get(),
                name = name,
            )
            out.base.create()
            return out
        }
    }
}