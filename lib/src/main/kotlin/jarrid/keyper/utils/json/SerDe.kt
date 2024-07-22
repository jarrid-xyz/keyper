package jarrid.keyper.utils.json

import InstantSerializer
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.Resource
import jarrid.keyper.tfcdk.DeploymentStack
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.util.*

class SerDe {
    private val module = SerializersModule {
        contextual(Instant::class, InstantSerializer)
        contextual(UUID::class, UUIDSerializer)
        contextual(Any::class, AnySerializer)
    }

    val json = Json {
        serializersModule = module
        encodeDefaults = true
    }

    fun <T> encode(serializer: KSerializer<T>, value: T): String {
        return json.encodeToString(serializer, value)
    }

    fun <T> decode(serializer: KSerializer<T>, string: String): T {
        return json.decodeFromString(serializer, string)
    }

    inline fun <reified T> encode(value: T): String {
        val serializer = when (T::class) {
            Model::class -> Model.serializer()
            Deployment::class -> Deployment.serializer()
            Resource::class -> Resource.serializer()
            DeploymentStack::class -> DeploymentStack.serializer()
            else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
        }
        return encode(serializer as KSerializer<T>, value)
    }

    inline fun <reified T> decode(string: String): T {
        val serializer = when (T::class) {
            Model::class -> Model.serializer()
            Deployment::class -> Deployment.serializer()
            Resource::class -> Resource.serializer()
            DeploymentStack::class -> DeploymentStack.serializer()
            else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
        }
        return decode(serializer as KSerializer<T>, string)
    }
}