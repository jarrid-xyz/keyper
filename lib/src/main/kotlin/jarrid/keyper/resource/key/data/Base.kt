package jarrid.keyper.resource.key.data

import com.google.protobuf.ByteString
import io.klogging.Klogging
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Config
import jarrid.keyper.app.Stack
import jarrid.keyper.resource.Model
import java.io.File
import java.nio.file.Files
import java.util.*

// Custom exception for unsupported stack type
class UnsupportedStackException(message: String = "") : Exception(message)


abstract class Base(
    val backend: Backend,
    val stack: Stack,
    val key: Model,
) : Klogging {

    private val app = Config().get()
    val provider = stack.getConfig(app)
    val projectId = provider.accountId
    val region = provider.region
    val keyName = getKeyName(key)

    fun run(
        input: String, output: File?,
        base64DecodeRead: Boolean = false,
        base64EncodeWrite: Boolean = true
    ): String? {
        return if (output != null) {
            input.toByteString(base64Decode = base64DecodeRead).run().write(output, base64Decode = base64EncodeWrite)
            null
        } else {
            input.toByteString(base64Decode = base64DecodeRead).run().toString(base64EncodeWrite)
        }
    }

    fun run(
        input: File,
        output: File?,
        base64DecodeRead: Boolean = false,
        base64EncodeWrite: Boolean = true
    ): String? {
        return if (output != null) {
            input.toByteString(base64Decode = base64DecodeRead).run().write(output, base64Decode = base64EncodeWrite)
            null
        } else {
            input.toByteString(base64Decode = base64DecodeRead).run().toString(base64EncodeWrite)
        }
    }

    private fun ByteString.write(output: File, base64Decode: Boolean = true) {
        if (base64Decode) {
            Files.write(output.toPath(), Base64.getEncoder().encode(this.toByteArray()))
        } else {
            Files.write(output.toPath(), this.toByteArray())
        }
    }

    private fun ByteString.toString(base64Encode: Boolean = true): String {
        return if (base64Encode) {
            this.toBase64String()
        } else {
            this.toStringUtf8()
        }
    }

    private fun String.toByteString(base64Decode: Boolean = false): ByteString {
        return if (base64Decode) {
            ByteString.copyFrom(Base64.getDecoder().decode(this))
        } else {
            ByteString.copyFromUtf8(this)
        }
    }

    private fun File.toByteString(base64Decode: Boolean = false): ByteString {
        val file = Files.readAllBytes(this.toPath())
        if (base64Decode) {
            return ByteString.copyFrom(Base64.getDecoder().decode(file))
        }
        return ByteString.copyFrom(file)
    }

    private fun ByteString.toBase64String(): String {
        return Base64.getEncoder().encodeToString(this.toByteArray())
    }

    abstract fun getKeyName(key: Model): Any

    abstract fun ByteString.run(): ByteString
}