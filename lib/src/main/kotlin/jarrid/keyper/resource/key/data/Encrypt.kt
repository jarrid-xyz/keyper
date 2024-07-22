package jarrid.keyper.resource.key.data

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.EncryptRequest
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import jarrid.keyper.resource.Backend
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Stack
import jarrid.keyper.resource.key.Name
import java.util.*
import jarrid.keyper.resource.Model as ResourceModel

class Encrypt(
    backend: Backend,
    stack: Stack,
    key: ResourceModel,
) : Base(backend, stack, key) {
    suspend fun run(plaintext: String): String {
        val key = getKeyResource()
        KeyManagementServiceClient.create().use { client ->
            // Build the key name
            val deployment = Deployment.get(key.base.id)
            val keyName =
                CryptoKeyName.of(
                    projectId,
                    region,
                    Name.getSanitizedName(deployment.base.id),
                    Name.getSanitizedName(key.base.id)
                )

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