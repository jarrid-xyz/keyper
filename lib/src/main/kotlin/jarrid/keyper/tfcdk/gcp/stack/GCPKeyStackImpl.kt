package jarrid.keyper.tfcdk.gcp.stack

import com.hashicorp.cdktf.LocalBackend
import com.hashicorp.cdktf.LocalBackendConfig
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKey
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKeyConfig
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRing
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRingConfig
import com.hashicorp.cdktf.providers.google.provider.GoogleProvider
import io.klogging.Klogging
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.key.Model
import jarrid.keyper.resource.key.Name
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.tfcdk.KeyStack
import jarrid.keyper.tfcdk.StackTfvars
import software.constructs.Construct

class GCPKeyStackImpl(
    scope: Construct,
) : Klogging, KeyStack(scope) {

    override suspend fun useBackend() {
        // Configure the local backend
        LocalBackend(
            this, LocalBackendConfig.builder()
                .path("terraform.tfstate")
                .build()
        )

    }

    override suspend fun useProvider() {
        val keyJsonPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
        logger.info("GCP provider credentials is set to $keyJsonPath")
        GoogleProvider.Builder.create(this, "Google")
            .credentials(keyJsonPath)
            .project(provider.accountId)
            .region(provider.region)
            .build()
    }

    private fun getLabels(key: Model, deployment: Deployment?): Map<String, String> {
        return mapOf(
            "stack-name" to stackName,
            "key-id" to key.base.id.toString(),
            "deployment-id" to deployment!!.base.id.toString()
        )
    }


    private fun createKmsKeyRing(keyRing: KeyRing): KmsKeyRing {
        return KmsKeyRing(
            this, keyRing.keyRingName, KmsKeyRingConfig.builder()
                .name(keyRing.keyRingName)
                .location(provider.region)
                .build()
        )
    }

    private fun createSymmetricKey(key: Key, keyRing: KmsKeyRing): KmsCryptoKey {
        // Define a KMS Crypto Key
        return KmsCryptoKey(
            this, key.keyName, KmsCryptoKeyConfig.builder()
                .name(key.keyName)
                .keyRing(keyRing.id)
                .rotationPeriod(key.rotationPeriod)
                .labels(key.labels)
                .build()
        )
    }

    override suspend fun create(tfvar: StackTfvars): List<Pair<KmsKeyRing, List<KmsCryptoKey>>> {
        if (tfvar !is Tfvars) {
            throw IllegalArgumentException("Expected tfvar of type Tfvars")
        }
        val out = tfvar.keyRings.map { keyRing ->
            val kmsKeyRing = createKmsKeyRing(keyRing)
            val keys = keyRing.keys.map { key ->
                createSymmetricKey(key, kmsKeyRing)
            }
            Pair(kmsKeyRing, keys)
        }
        logger.info("Created GCP terraform stack: $out")
        return out
    }

    override fun convert(stack: List<DeploymentStack>): Tfvars {
        val keyRings: MutableList<KeyRing> = mutableListOf()
        // TODO: fix it
        for (config in stack) {
            val tfKeys: MutableList<Key> = mutableListOf()
            for (key in config.keys) {
                val keyId = key.base.id
                tfKeys.add(
                    Key(
                        keyName = Name.getSanitizedName(keyId),
                        keyId = keyId,
                        rotationPeriod = getKeyConfigOptions(key, "rotationPeriod"),
//                        TODO: fix it
                        labels = getLabels(key, null)
                    )
                )
            }
            keyRings.add(
                KeyRing(
                    keyRingName = Name.getSanitizedName(config.deployment.base.id),
                    deploymentId = config.deployment.base.id,
                    keys = tfKeys
                )
            )
        }

        return Tfvars(
            stackName = stackName,
            region = provider.region,
            keyRings = keyRings
        )
    }
}