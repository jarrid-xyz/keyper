package jarrid.keyper.resource

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
    val region: String
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
        override fun get(): FileBackend = Local(Config())
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
        override fun getConfig(config: App): CloudProviderConfig? = config.provider.gcp
    };

//    @SerialName("aws")
//    AWS {
//        override fun get(): KClass<out KeyStack> = AWSKeyStackImpl::class
//        override fun getConfig(config: App): CloudProviderConfig? = provider.aws
//    };

    abstract fun get(): KClass<out KeyStack>
    abstract fun getConfig(config: App): CloudProviderConfig?
}

@Serializable
data class BackendConfig(
    val backend: Backend = Backend.LOCAL,
    val path: String
)

@Serializable
data class File(
    val file: BackendConfig
)

@Serializable
data class App(
    val provider: ProviderConfig = ProviderConfig(),
    val manager: File,
    @SerialName("out_dir")
    val outDir: String = "./"
)

class Config(path: String = "/app.yaml") {
    private val stream: InputStream =
        Config::class.java.getResourceAsStream(path)
            ?: throw IllegalArgumentException("App config file not found: $path")

    private fun load(): App {
        return Yaml.default.decodeFromStream(App.serializer(), stream)
    }

    private var config: App = load()
    fun get(): App {
        return config
    }
}