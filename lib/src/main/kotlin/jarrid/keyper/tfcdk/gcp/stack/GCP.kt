package jarrid.keyper.tfcdk.gcp.stack

import com.hashicorp.cdktf.LocalBackend
import com.hashicorp.cdktf.LocalBackendConfig
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKey
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKeyConfig
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRing
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRingConfig
import com.hashicorp.cdktf.providers.google.provider.GoogleProvider
import com.hashicorp.cdktf.providers.google.service_account.ServiceAccount
import com.hashicorp.cdktf.providers.google.service_account.ServiceAccountConfig
import io.klogging.Klogging
import jarrid.keyper.resource.CloudProviderConfig
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.key.Name
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.tfcdk.Stack
import software.constructs.Construct
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key
import jarrid.keyper.tfcdk.gcp.stack.Key as TfKey

class GCP(
    scope: Construct,
    stackName: String,
) : Klogging, Stack(scope, stackName = stackName) {

    private val provider: CloudProviderConfig
        get() {
            return config.provider.gcp
        }

    override suspend fun useBackend() {
        // Configure the local backend
        LocalBackend(
            this, LocalBackendConfig.builder()
                .path("terraform.tfstate")
                .build()
        )

    }

    override suspend fun useProvider() {
        val credJsonPath = System.getenv("GOOGLE_CLOUD_KEYFILE_JSON")
        logger.info("GCP provider credentials is set to $credJsonPath")
        GoogleProvider.Builder.create(this, "Google")
            .credentials(credJsonPath)
            .project(provider.accountId)
            .region(provider.region)
            .build()
    }

    private fun getLabels(key: Key, deployment: Deployment): Map<String, String> {
        return mapOf(
            "stack-name" to stackName,
            "key-id" to key.base.id.toString(),
            "deployment-id" to deployment.base.id.toString()
        )
    }

    private fun createServiceAccount(role: Role, deployment: Deployment): ServiceAccount {
        return ServiceAccount(
            this, role.base.name!!, ServiceAccountConfig.builder()
                .accountId(Name.getSanitizedAccountId(role.base.name))
                .displayName(role.base.name)
                .description("jarrid-keyper service account for ${role.base.name}. Deployment")
                .build()
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

    private fun createSymmetricKey(key: TfKey, keyRing: KmsKeyRing): KmsCryptoKey {
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

    private fun createKeys(tfvar: DeploymentStack) {
        val keyRingPayload = KeyRing(
            deploymentId = tfvar.deployment.id,
            keyRingName = tfvar.deployment.name
        )
        val kmsKeyRing = createKmsKeyRing(keyRingPayload)
        tfvar.keys.forEach { key ->
            val keyPayload = TfKey(
                keyName = key.base.name ?: Name.getJarridKeyName(key.base.id),
                keyId = key.base.id,
                ttl = key.ttl,
                rotationPeriod = key.rotationPeriod,
                labels = getLabels(key, deployment = tfvar.deployment)
            )
            createSymmetricKey(keyPayload, kmsKeyRing)
        }
    }

    private fun createRoles(tfvar: DeploymentStack) {
        tfvar.roles.forEach { role ->
            createServiceAccount(role, tfvar.deployment)
        }
    }

    override suspend fun create(tfvar: DeploymentStack) {
        createKeys(tfvar)
        createRoles(tfvar)
        logger.info("Created GCP terraform stack")
    }
}