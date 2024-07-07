package jarrid.keyper.utils.model

import java.util.*

object NewUUID {
    @JvmStatic
    fun get(): UUID {
        return UUID.randomUUID()
    }
}

fun String.toUUID(): UUID {
    return try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("$this is not a valid UUID")
    }
}