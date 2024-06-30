package jarrid.keyper.utils.model

import java.util.*

object NewUUID {
    @JvmStatic
    fun get(): UUID {
        return UUID.randomUUID()
    }
}
