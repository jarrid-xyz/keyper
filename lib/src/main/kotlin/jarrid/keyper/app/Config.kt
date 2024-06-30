package jarrid.keyper.app

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import jarrid.keyper.tfcdk.KeyStack
import jarrid.keyper.tfcdk.gcp.stack.GCPKeyStackImpl
import jarrid.keyper.utils.file.Local
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.InputStream
import kotlin.reflect.KClass
import jarrid.keyper.utils.file.Backend as FileBackend

@Serializable
data class CloudProviderConfig(
    val accountId: String,
    val region: String = "us-east-1"
)

@Serializable
data class ProviderConfig(
    val gcp: CloudProviderConfig? = null,
    val aws: CloudProviderConfig? = null
)

@Serializable
enum class Backend {
    @SerialName("local")
    LOCAL {
        override fun get(): FileBackend = Local()
    },

    @SerialName("s3")
    S3 {
        override fun get(): FileBackend {
            TODO("Not yet implemented")
        }
    },

    @SerialName("gcs")
    GCS {
        override fun get(): FileBackend {
            TODO("Not yet implemented")
        }
    };

    abstract fun get(): FileBackend
}

@Serializable
enum class Stack {
    @SerialName("gcp")
    GCP {
        override fun get(): KClass<out KeyStack> = GCPKeyStackImpl::class
    };

    abstract fun get(): KClass<out KeyStack>
}

@Serializable
data class BackendConfig(
    val backend: Backend = Backend.LOCAL,
    val path: String = "configs"
)

@Serializable
data class File(
    val file: BackendConfig
)

@Serializable
data class AppConfig(
    val provider: ProviderConfig,
    val manager: File
)

class Config(path: String = "/app.yaml") {
    private val stream: InputStream =
        Config::class.java.getResourceAsStream(path)
            ?: throw IllegalArgumentException("File not found: app.yaml")

    private var config: AppConfig = load()
    private fun load(): AppConfig {
        return Yaml.default.decodeFromStream(AppConfig.serializer(), stream)
    }

    fun get(): AppConfig {
        return config
    }
}