package jarrid.keyper.resource.key.data.gcp

import com.google.cloud.kms.v1.DecryptRequest
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.data.Base

class Decrypt(
    backend: Backend,
    stack: Stack,
    key: Model,
) : Base(backend, stack, key) {
    override fun ByteString.run(): ByteString {
        KeyManagementServiceClient.create().use { client ->
            val request = DecryptRequest.newBuilder()
                .setName(keyName.toString())
                .setCiphertext(this)
                .build()
            val response = client.decrypt(request)
            return response.plaintext
        }
    }

    override fun getKeyName(key: Model): Any {
        TODO("Not yet implemented")
    }
}