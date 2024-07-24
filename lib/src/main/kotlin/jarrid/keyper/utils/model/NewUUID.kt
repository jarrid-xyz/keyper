package jarrid.keyper.utils.model

import java.util.*

object NewUUID {
    @JvmStatic
    fun get(): UUID = UUID.randomUUID()

    @JvmStatic
    fun getEmpty(): UUID = UUID(0L, 0L)
}

fun UUID.isEmpty(): Boolean = this == UUID(0L, 0L)

class InvalidUUIDException(message: String) : Exception(message)

fun String.toUUID(): UUID {
    return try {
        UUID.fromString(this)
    } catch (e: IllegalArgumentException) {
        throw InvalidUUIDException("$this is not a valid UUID")
    }
}
