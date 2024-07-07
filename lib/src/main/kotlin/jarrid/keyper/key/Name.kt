package jarrid.keyper.key

import java.util.*

object Name {
    fun getSanitizedName(uuid: UUID): String {
        return "jarrid-keyper-key-$uuid"
    }
}
