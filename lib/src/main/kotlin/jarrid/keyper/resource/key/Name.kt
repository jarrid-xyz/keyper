package jarrid.keyper.resource.key

import java.util.*

object Name {
    fun getJarridKeyName(uuid: UUID): String {
        return "jarrid-keyper-key-$uuid"
    }
}
