package jarrid.keyper.key

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*


enum class Usage {
    CREATE_KEY,
    SHARE_KEY,
    ENCRYPT,
    DECRYPT
}


@Serializable
data class Model(
    val usage: Usage,
    @Contextual var keyName: String? = null,
    @Contextual var keyId: UUID? = null,
    //  in days, -1 = infinite, 0 = destroy after one use
    val ttl: Int = 7,
    @Contextual var created: Instant,
    @Contextual var updated: Instant? = null,
    @Contextual var deploymentId: UUID? = null,
    @Contextual val context: Map<String, @Contextual Any>? = null,
)

