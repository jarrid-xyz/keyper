package jarrid.keyper.resource.key.data

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.protobuf.ByteString
import io.klogging.Klogging
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Config
import jarrid.keyper.app.Stack
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.Name
import java.io.File
import java.nio.file.Files
import java.util.*

abstract class Base(
    val backend: Backend,
    val stack: Stack,
    val key: Model,
) : Klogging {

    private val app = Config().get()
    val provider = stack.getConfig(app)
    private val projectId = provider.accountId
    private val region = provider.region
    val keyName = getKeyName(key)

    fun run(input: String): String {
        return input.toByteString().run().toStringUtf8()
    }

    fun run(input: String, output: File) {
        input.toByteString().run().write(output)
    }

    fun run(input: File): String {
        return input.toByteString().run().toStringUtf8()
    }

    fun run(input: File, output: File) {
        input.toByteString().run().write(output)
    }

    fun ByteString.write(output: File) {
        Files.write(output.toPath(), Base64.getEncoder().encode(this.toByteArray()))
    }

    fun String.toByteString(): ByteString {
        return ByteString.copyFrom(Base64.getDecoder().decode(this))
    }

    fun File.toByteString(): ByteString {
        return ByteString.copyFrom(Base64.getDecoder().decode(Files.readAllBytes(this.toPath())))
    }

    fun ByteString.toBase64String(): String {
        return Base64.getEncoder().encodeToString(this.toByteArray())
    }

    fun getKeyName(key: Model): CryptoKeyName {
        return CryptoKeyName.of(
            projectId,
            region,
            key.deployment.name,
            Name.getJarridKeyName(key.resource.base)
        )
    }

    abstract fun ByteString.run(): ByteString
}