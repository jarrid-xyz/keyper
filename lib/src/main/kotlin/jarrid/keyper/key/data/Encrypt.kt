package jarrid.keyper.key.data

import com.google.cloud.kms.v1.CryptoKeyName
import com.google.cloud.kms.v1.EncryptRequest
import com.google.cloud.kms.v1.KeyManagementServiceClient
import com.google.protobuf.ByteString
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.key.Name
import java.util.*

class Encrypt(
    backend: Backend,
    stack: Stack,
    deploymentId: UUID?,
    keyId: UUID
) : Base(backend, stack, deploymentId, keyId) {
    suspend fun run(plaintext: String): String {
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