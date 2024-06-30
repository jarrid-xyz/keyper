package jarrid.keyper.tfcdk.gcp.stack

import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKey
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKeyConfig
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRing
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRingConfig
import com.hashicorp.cdktf.providers.google.provider.GoogleProvider
import io.klogging.Klogging
import jarrid.keyper.key.Model
import jarrid.keyper.tfcdk.KeyStack
import jarrid.keyper.tfcdk.StackTfvars
import software.constructs.Construct
import java.util.*

class GCPKeyStackImpl(
    scope: Construct,
    private val terraformId: UUID,
) : Klogging, KeyStack(scope, terraformId) {

    // TODO: there's better ways
    private val projectId: String
        get() = appConfig.provider.gcp!!.accountId
    private val region: String
        get() = appConfig.provider.gcp!!.region

//    private val projectId = getProjectId()
//    private val region = getRegion()
//    private fun getProjectId(): String {
//        return appConfig.provider.gcp!!.accountId
//    }
//
//    private fun getRegion(): String {
//        return appConfig.provider.gcp!!.region
//    }


    override fun useProvider() {
        GoogleProvider.Builder.create(this, "Google")
            .project(projectId)
            .region(region)
            .build()
    }

    private fun getLabels(key: Key): Map<String, String> {
        return mapOf(
            "keyId" to key.keyId.toString(),
            "deploymentId" to terraformId.toString()
        )
    }


    private fun createKmsKeyRing(keyRing: KeyRing): KmsKeyRing {
        return KmsKeyRing(
            this, keyRing.keyRingName, KmsKeyRingConfig.builder()
                .name(keyRing.keyRingName)
                .location(region)
                .build()
        )
    }

    private fun createSymmetricKey(key: Key, keyRing: KmsKeyRing): KmsCryptoKey {
        // Define a KMS Crypto Key
        return KmsCryptoKey(
            this, key.keyName, KmsCryptoKeyConfig.builder()
                .name(key.keyName)
                .keyRing(keyRing.name)
                .rotationPeriod(key.rotationPeriod)
                .labels(getLabels(key))
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

    override fun convert(configs: List<Model>): Tfvars {
        val keys: List<Key> = configs.map { config ->
            val keyId = config.keyId!!
            Key(
                keyName = getSanitizedName(keyId),
                keyId = keyId,
                rotationPeriod = getKeyConfigOptions(config, "rotationPeriod")

            )
        }
        return Tfvars(
            deploymentId = terraformId,
            region = region,
            keyRings = listOf(
                KeyRing(
                    keyRingName = getSanitizedName(terraformId),
                    keys = keys
                )
            )
        )
    }
}