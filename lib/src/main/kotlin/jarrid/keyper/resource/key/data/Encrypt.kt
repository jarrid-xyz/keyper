package jarrid.keyper.resource.key.data

import com.google.cloud.kms.v1.EncryptRequest
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import java.util.*
import jarrid.keyper.resource.Model as ResourceModel

class Encrypt(
    backend: Backend,
    stack: Stack,
    key: ResourceModel,
) : Base(backend, stack, key) {
    fun run(plaintext: String): String {
        KeyManagementServiceClient.create().use { client ->
            val keyName = getKeyName(key)

            // Convert plaintext to ByteString
            val plaintextByteString = ByteString.copyFromUtf8(plaintext)

            // Build the encrypt request
            val request = EncryptRequest.newBuilder()
                .setName(keyName.toString())
                .setPlaintext(plaintextByteString)
                .build()

            // Encrypt the data
            val response = client.encrypt(request)

            // Get the ciphertext
            val ciphertext = response.ciphertext

            // Return the Base64 encoded ciphertext
            return Base64.getEncoder().encodeToString(ciphertext.toByteArray())
        }
    }
}