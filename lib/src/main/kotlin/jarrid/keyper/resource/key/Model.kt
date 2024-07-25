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
data class Permission(
    val allowEncrypt: List<String> = emptyList(),
    val allowDecrypt: List<String> = emptyList()
)

fun Permission.edit(
    type: EditPermission,
    roles: List<String>
): Permission {
    return when (type) {
        EditPermission.ADD_ALLOW_ENCRYPT -> this.copy(allowEncrypt = (this.allowEncrypt + roles).toSortedSet().toList())
        EditPermission.ADD_ALLOW_DECRYPT -> this.copy(allowDecrypt = (this.allowDecrypt + roles).toSortedSet().toList())
        EditPermission.REMOVE_ALLOW_ENCRYPT -> this.copy(
            allowEncrypt = (this.allowEncrypt - roles.toSet()).toSortedSet().toList()
        )

        EditPermission.REMOVE_ALLOW_DECRYPT -> this.copy(
            allowDecrypt = (this.allowDecrypt - roles.toSet()).toSortedSet().toList()
        )
    }
}

enum class EditPermission {
    ADD_ALLOW_ENCRYPT,
    ADD_ALLOW_DECRYPT,
    REMOVE_ALLOW_ENCRYPT,
    REMOVE_ALLOW_DECRYPT,
}

@Serializable
data class Model(
    @Transient private val id: UUID = NewUUID.getEmpty(),
    @Transient private val name: String? = null,
    @Transient private val context: Map<String, Any>? = null,

    val ttl: Int = 7,
    val rotationPeriod: String = "7776000s", // 90 days
    var permission: Permission = Permission()
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
                rotationPeriod = payload.getConfigAttribute("rotation_period", "7776000s") as String,
                permission = Permission()
            )
            out.base.create()
            return out
        }

        fun get(id: UUID): Model {
            return Model(id = id)
        }
    }

    fun editPermission(edit: EditPermission, role: List<String>) {
        this.permission = permission.edit(edit, role)
        this.base.updated()
    }
}