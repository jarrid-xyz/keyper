package jarrid.keyper.utils.json

import jarrid.keyper.resource.Base
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.Resource
import jarrid.keyper.tfcdk.DeploymentStack
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import java.time.Instant
import java.util.*
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

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

    inline fun <reified T> getSerializer(): KSerializer<T> {
        val serializer = when (T::class) {
            Base::class -> Base.serializer()
            Model::class -> Model.serializer()
            Key::class -> Key.serializer()
            Role::class -> Role.serializer()
            Deployment::class -> Deployment.serializer()
            Resource::class -> Resource.serializer()
            DeploymentStack::class -> DeploymentStack.serializer()
            else -> throw IllegalArgumentException("Unsupported type: ${T::class}")
        }
        return serializer as KSerializer<T>
    }


    inline fun <reified T> encode(value: T): String {
        val serializer = getSerializer<T>()
        return encode(serializer, value)
    }

    inline fun <reified T> decode(string: String): T {
        val serializer = getSerializer<T>()
        return decode(serializer, string)
    }
}