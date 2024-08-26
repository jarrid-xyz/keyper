package jarrid.keyper.resource.key.data.aws

import com.amazonaws.services.kms.AWSKMS
import com.amazonaws.services.kms.AWSKMSClientBuilder
import com.amazonaws.services.kms.model.EncryptRequest
import com.amazonaws.services.kms.model.EncryptResult
import com.google.protobuf.ByteString
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.resource.key.data.Base
import jarrid.keyper.resource.Model as ResourceModel

class Encrypt(
    backend: Backend,
    stack: Stack,
    key: ResourceModel,
) : Base(backend, stack, key) {
    override fun ByteString.run(): ByteString {
        val client: AWSKMS = AWSKMSClientBuilder.defaultClient()
        val request = EncryptRequest()
            .withKeyId(keyName.toString())
            .withPlaintext(this.asReadOnlyByteBuffer()) // Convert ByteString to ByteBuffer without copying

        val response: EncryptResult = client.encrypt(request)
        return ByteString.copyFrom(response.ciphertextBlob) // Convert ByteBuffer to ByteString
    }
}