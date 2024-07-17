package jarrid.keyper.utils.json

import jarrid.keyper.resource.key.Model
import kotlinx.serialization.json.Json

fun encode(config: Model): String {
    val json = Json {
        serializersModule = module
    }
    val encoded: String = json.encodeToString(Model.serializer(), config)
    return encoded
}

fun decode(string: String): Model {
    val json = Json {
        serializersModule = module
    }
    val decoded: Model = json.decodeFromString(Model.serializer(), string)
    return decoded
}