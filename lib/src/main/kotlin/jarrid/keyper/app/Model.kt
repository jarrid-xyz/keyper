package jarrid.keyper.app

import jarrid.keyper.utils.file.Local
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
data class CloudProviderConfig(
    val accountId: String = "",
    val region: String = "global",
    val backend: TfBackend = TfBackend(),
    val credentials: String = "",
)

enum class TfBackendType {
    @SerialName("local")
    LOCAL,

    @SerialName("cloud")
    CLOUD

}

@Serializable
data class TfBackend(
    val type: TfBackendType = TfBackendType.LOCAL,
    val path: String = "terraform.tfstate",
    val bucket: String = "tf-state",
)

@Serializable
data class Tfcdk(
    val stack: Stack = Stack.GCP,
    val path: String = "cdktf.out",
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
        override fun get(): jarrid.keyper.utils.file.Backend = Local(Config())
    },

    @SerialName("s3")
    S3 {
        override fun get(): jarrid.keyper.utils.file.Backend {
            TODO("Not yet implemented")
        }
    },

    @SerialName("gcs")
    GCS {
        override fun get(): jarrid.keyper.utils.file.Backend {
            TODO("Not yet implemented")
        }
    };

    abstract fun get(): jarrid.keyper.utils.file.Backend
}

@Serializable
enum class Stack {
    @SerialName("gcp")
    GCP {
        override fun get(): KClass<out jarrid.keyper.tfcdk.Stack> = jarrid.keyper.tfcdk.gcp.stack.GCP::class
        override fun getConfig(config: App): CloudProviderConfig = config.provider.gcp
    };

//    @SerialName("aws")
//    AWS {
//        override fun get(): KClass<out KeyStack> = AWSKeyStackImpl::class
//        override fun getConfig(config: App): CloudProviderConfig? = provider.aws
//    };

    abstract fun get(): KClass<out jarrid.keyper.tfcdk.Stack>
    abstract fun getConfig(config: App): CloudProviderConfig
}

@Serializable
data class BackendConfig(
    val backend: Backend = Backend.LOCAL,
    val path: String = "config"
)

@Serializable
data class ResourceBackend(
    val backend: BackendConfig
)

@Serializable
enum class ENV {
    @SerialName("local")
    LOCAL,

    @SerialName("development")
    DEVELOPMENT,

    @SerialName("staging")
    STAGING,

    @SerialName("production")
    PRODUCTION
}

@Serializable
data class App(
    val provider: ProviderConfig = ProviderConfig(),
    val resource: ResourceBackend,
    val env: ENV = ENV.LOCAL,
    @SerialName("out_dir")
    val outDir: String = "./"
)