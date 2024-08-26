package jarrid.keyper.resource.key.data.aws

import com.google.protobuf.ByteString
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Stack
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.Name
import jarrid.keyper.resource.key.data.Base
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.DecryptRequest

class Decrypt(
    backend: Backend,
    stack: Stack,
    key: Model,
) : Base(backend, stack, key) {

    override fun ByteString.run(): ByteString {
        val client: KmsClient = KmsClient.create()

        // Convert ByteString to SdkBytes
        val sdkBytes = SdkBytes.fromByteArray(this.toByteArray())

        val request = DecryptRequest.builder()
            .ciphertextBlob(sdkBytes)  // Use SdkBytes for ciphertext
            .keyId(keyName as String)  // Use the key alias or ARN
            .build()

        val response = client.decrypt(request)
        // Convert SdkBytes back to ByteString
        return ByteString.copyFrom(response.plaintext().asByteArray())
    }

    override fun getKeyName(key: Model): String {
        val name = Name.getJarridKeyName(key.resource.base)
        return "alias/$name"
    }
}