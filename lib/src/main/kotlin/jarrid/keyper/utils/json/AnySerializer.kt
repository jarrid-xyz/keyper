package jarrid.keyper.utils.json

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*

@OptIn(InternalSerializationApi::class)
object AnySerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("Any", JsonElement.serializer().descriptor.kind)

    override fun serialize(encoder: Encoder, value: Any) {
        when (value) {
            is Boolean -> encoder.encodeBoolean(value)
            is Byte -> encoder.encodeByte(value)
            is Short -> encoder.encodeShort(value)
            is Int -> encoder.encodeInt(value)
            is Long -> encoder.encodeLong(value)
            is Float -> encoder.encodeFloat(value)
            is Double -> encoder.encodeDouble(value)
            is Char -> encoder.encodeChar(value)
            is String -> encoder.encodeString(value)
            else -> encoder.encodeSerializableValue(JsonElement.serializer(), Json.encodeToJsonElement(value))
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        return when (val element = decoder.decodeSerializableValue(JsonElement.serializer())) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.booleanOrNull != null -> element.boolean
                    element.longOrNull != null -> element.long
                    element.doubleOrNull != null -> element.double
                    else -> throw IllegalArgumentException("Unsupported JSON primitive")
                }
            }

            else -> element
        }
    }
}
