package jarrid.keyper.resource.iam

import kotlinx.serialization.Contextual
import java.util.UUID

data class Model (
    val name: String,
    val serviceAccountId: UUID,
    @Contextual val context: Map<String, @Contextual Any>? = null,
)