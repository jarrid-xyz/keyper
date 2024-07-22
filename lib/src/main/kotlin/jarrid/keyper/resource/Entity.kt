package jarrid.keyper.resource

import jarrid.keyper.utils.model.NewUUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.time.Instant
import java.util.*

enum class ResourceType {
    DEPLOYMENT,
    KEY,
    ROLE,
}

@Serializable
class Base(
    @Contextual val created: Instant? = null,
    @Contextual val updated: Instant? = null,
    @Contextual val id: UUID,
    val name: String? = null,
    val context: Map<String, @Contextual Any>? = null,
)

@Serializable
open class Resource(
    val base: Base,
    val type: ResourceType
)

@Serializable
data class Deployment(
    @Transient
    private val _id: UUID = NewUUID.getEmpty(),
    @Transient
    private val _name: String = "default",
    @Transient
    private val _context: Map<String, @Contextual Any>? = null,
) : Resource(
    base = Base(id = _id, name = _name, context = _context),
    type = ResourceType.DEPLOYMENT
) {
    @Contextual
    val id: UUID
        get() = base.id

    val name: String
        get() = base.name ?: _name

    val context: Map<String, @Contextual Any>?
        get() = base.context ?: _context

    companion object {
        // Factory method to create a new Deployment with a new ID
        fun new(
            name: String = "default",
            context: Map<String, @Contextual Any>? = null
        ): Deployment {
            return Deployment(
                _id = NewUUID.get(),
                _name = name,
                _context = context
            )
        }

        fun get(id: UUID, name: String = "default"): Deployment {
            return Deployment(
                _id = id,
                _name = name,
            )
        }
    }
}