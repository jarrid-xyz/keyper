package jarrid.keyper.app

import jarrid.keyper.tfcdk.Stack
import jarrid.keyper.utils.file.Local
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass
import jarrid.keyper.app.Stack as AppStack
import jarrid.keyper.utils.file.Backend as FileBackend

@Serializable
data class CloudProviderConfig(
    val accountId: String = "",
    val region: String = "global",
    val backend: TfBackend = TfBackend(),
    val credentials: String = "",
    @SerialName("assume_role_arn")
    val assumeRoleArn: String = ""
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
    val bucket: String = "keyper-tf-state",
)

@Serializable
data class Tfcdk(
    val stack: AppStack = AppStack.GCP,
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
        override fun get(): KClass<out Stack> = jarrid.keyper.tfcdk.stack.gcp.GCP::class
        override fun getConfig(config: App): CloudProviderConfig = config.provider.gcp
    },

    @SerialName("aws")
    AWS {
        override fun get(): KClass<out Stack> = jarrid.keyper.tfcdk.stack.aws.AWS::class
        override fun getConfig(config: App): CloudProviderConfig = config.provider.aws
    };

    abstract fun get(): KClass<out Stack>
    abstract fun getConfig(config: App): CloudProviderConfig
}

@Serializable
data class BackendConfig(
    val backend: Backend = Backend.LOCAL,
    val path: String = "configs"
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