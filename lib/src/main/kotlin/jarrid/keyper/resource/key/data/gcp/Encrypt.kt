package jarrid.keyper.resource.key.data.gcp

import com.google.cloud.kms.v1.EncryptRequest
import com.google.cloud.kms.v1.KeyManagementServiceClient
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
        KeyManagementServiceClient.create().use { client ->
            val request = EncryptRequest.newBuilder()
                .setName(keyName.toString())
                .setPlaintext(this)
                .build()

            val response = client.encrypt(request)
            return response.ciphertext
        }
    }
}
