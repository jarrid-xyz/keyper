package jarrid.keyper.resource

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import jarrid.keyper.utils.file.Local
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.InputStream
import kotlin.reflect.KClass
import jarrid.keyper.tfcdk.Stack as TfStack
import jarrid.keyper.tfcdk.gcp.stack.GCP as GCPStack
import jarrid.keyper.utils.file.Backend as FileBackend

@Serializable
data class CloudProviderConfig(
    val accountId: String = "",
    val region: String = "global"
)

@Serializable
data class Tfcdk(
    val stack: Stack = Stack.GCP,
    val path: String = "cdktf.out"
)

@Serializable
data class ProviderConfig(
    val gcp: CloudProviderConfig = CloudProviderConfig(),
    val aws: CloudProviderConfig = CloudProviderConfig(),
    val tfcdk: Tfcdk = Tfcdk()
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
        override fun get(): KClass<out TfStack> = GCPStack::class
        override fun getConfig(config: App): CloudProviderConfig = config.provider.gcp
    };

//    @SerialName("aws")
//    AWS {
//        override fun get(): KClass<out KeyStack> = AWSKeyStackImpl::class
//        override fun getConfig(config: App): CloudProviderConfig? = provider.aws
//    };

    abstract fun get(): KClass<out TfStack>
    abstract fun getConfig(config: App): CloudProviderConfig
}

@Serializable
data class BackendConfig(
    val backend: Backend = Backend.LOCAL,
    val path: String
)

@Serializable
data class ResourceBackend(
    val backend: BackendConfig
)

@Serializable
data class App(
    val provider: ProviderConfig = ProviderConfig(),
    val resource: ResourceBackend,
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