package jarrid.keyper.key.data

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.DecryptRequest
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.key.Name
import java.util.*

class Decrypt(
    backend: Backend,
    stack: Stack,
    deploymentId: UUID?,
    keyId: UUID
) : Base(backend, stack, deploymentId, keyId) {
    suspend fun run(ciphertext: String): String {
        val key = getKeyConfig()
        KeyManagementServiceClient.create().use { client ->
            // Build the key name
            val keyName =
                CryptoKeyName.of(
                    projectId,
                    region,
                    Name.getSanitizedName(key.deploymentId!!),
                    Name.getSanitizedName(key.keyId!!)
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