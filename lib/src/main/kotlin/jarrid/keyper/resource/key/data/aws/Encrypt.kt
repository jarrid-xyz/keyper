package jarrid.keyper.resource.key.data.aws

import com.google.protobuf.ByteString
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.resource.key.Name
import jarrid.keyper.resource.key.data.Base
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.EncryptRequest
import jarrid.keyper.resource.Model as ResourceModel

class Encrypt(
    backend: Backend,
    stack: Stack,
    key: ResourceModel,
) : Base(backend, stack, key) {

    override fun ByteString.run(): ByteString {
        val client: KmsClient = KmsClient.create()
        val sdkBytes = SdkBytes.fromByteBuffer(this.asReadOnlyByteBuffer())

        val request = EncryptRequest.builder()
            .keyId(keyName as String)  // Use the key alias
            .plaintext(sdkBytes)  // Convert ByteString to ByteBuffer without copying
            .build()

        val response = client.encrypt(request)
        return ByteString.copyFrom(response.ciphertextBlob().asByteArray())
    }

    override fun getKeyName(key: ResourceModel): String {
        val name = Name.getJarridKeyName(key.resource.base)
        return "alias/$name"
    }
}