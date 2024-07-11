package jarrid.keyper.tfcdk.gcp.stack

import jarrid.keyper.tfcdk.StackTfvars
import java.util.*

data class Tfvars(
    override val stackName: String,
    override val region: String,
    val keyRings: List<KeyRing>
) : StackTfvars

data class KeyRing(
    val deploymentId: UUID,
    val keyRingName: String,
    val keys: List<Key>

)

data class Key(
    val keyName: String,
    val keyId: UUID,
    val rotationPeriod: String? = "7776000s",
    val labels: Map<String, String>
)