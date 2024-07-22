package jarrid.keyper.resource.key.data

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.DecryptRequest
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import jarrid.keyper.resource.Backend
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.Stack
import jarrid.keyper.resource.key.Name
import java.util.*

class Decrypt(
    backend: Backend,
    stack: Stack,
    key: Model,
) : Base(backend, stack, key) {
    suspend fun run(ciphertext: String): String {
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

            // Decode the Base64 encoded ciphertext
            val ciphertextByteString = ByteString.copyFrom(Base64.getDecoder().decode(ciphertext))

            // Build the decrypt request
            val request = DecryptRequest.newBuilder()
                .setName(keyName.toString())
                .setCiphertext(ciphertextByteString)
                .build()

            // Decrypt the data
            val response = client.decrypt(request)

            // Get the plaintext
            val plaintext = response.plaintext

            // Return the plaintext as a string
            return plaintext.toStringUtf8()
        }
    }
}