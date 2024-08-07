package jarrid.keyper.resource.iam

import jarrid.keyper.resource.Base
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Resource
import jarrid.keyper.resource.ResourceType
import jarrid.keyper.utils.model.NewUUID
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

class RoleNameIsUndefinedException(message: String = "Role name must be defined in the payload") :
    Exception(message)


@Serializable
data class Model(
    @Transient private val id: UUID = NewUUID.getEmpty(),
    @Transient private val name: String = "role",
    @Transient private val context: Map<String, Any>? = null,
) : Resource(
    base = Base(id = id, name = name, context = context),
    type = ResourceType.ROLE,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Model) return false
        return this.base.id == other.base.id
    }

    override fun hashCode(): Int {
        return this.base.id.hashCode()
    }
    
    companion object {
        fun create(payload: BasePayload): Model {
            val name = payload.name ?: throw RoleNameIsUndefinedException()

            val out = Model(
                id = payload.id ?: NewUUID.get(),
                name = name,
                context = payload.context
            )
            out.base.create()
            return out
        }
    }
}