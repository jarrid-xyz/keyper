package jarrid.keyper.utils.json

import InstantSerializer
import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.util.*

val module = SerializersModule {
    contextual(Instant::class, InstantSerializer)
    contextual(UUID::class, UUIDSerializer)
    contextual(Any::class, AnySerializer)
}

