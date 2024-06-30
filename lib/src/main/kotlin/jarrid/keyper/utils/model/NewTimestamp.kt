package jarrid.keyper.utils.model

import java.time.Clock
import java.time.Instant

object NewTimestamp {
    @JvmStatic
    fun get(): Instant {
        return Clock.systemUTC().instant()
    }
}