package jarrid.keyper.resource.key.data.aws

import com.amazonaws.services.kms.AWSKMS
import com.amazonaws.services.kms.AWSKMSClientBuilder
import com.amazonaws.services.kms.model.DecryptRequest
import com.google.protobuf.ByteString
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.data.Base
import java.nio.ByteBuffer

class Decrypt(
    backend: Backend,
    stack: Stack,
    key: Model,
) : Base(backend, stack, key) {
    override fun ByteString.run(): ByteString {
        val client: AWSKMS = AWSKMSClientBuilder.defaultClient()
        val request = DecryptRequest()
            .withCiphertextBlob(ByteBuffer.wrap(this.toByteArray())) // Convert ByteString to ByteBuffer
            .withKeyId(keyName.toString())
        val response = client.decrypt(request)
        return ByteString.copyFrom(response.plaintext) // Convert ByteBuffer to ByteString
    }
}