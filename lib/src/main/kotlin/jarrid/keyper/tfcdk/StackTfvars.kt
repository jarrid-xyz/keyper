package jarrid.keyper.tfcdk

import java.util.*

interface StackTfvars {
    val region: String
    val deploymentId: UUID
}
