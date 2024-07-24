package jarrid.keyper.tfcdk.gcp.stack

import java.util.*

data class KeyRing(
    val deploymentId: UUID,
    val keyRingName: String,
)

data class Key(
    val keyName: String,
    val keyId: UUID,
    val ttl: Int,
    val rotationPeriod: String? = "7776000s",
    val labels: Map<String, String>
)