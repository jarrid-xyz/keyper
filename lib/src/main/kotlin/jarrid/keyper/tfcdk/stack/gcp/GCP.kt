package jarrid.keyper.tfcdk.stack.gcp

import com.hashicorp.cdktf.GcsBackend
import com.hashicorp.cdktf.GcsBackendConfig
import com.hashicorp.cdktf.providers.google.data_google_iam_policy.DataGoogleIamPolicy
import com.hashicorp.cdktf.providers.google.data_google_iam_policy.DataGoogleIamPolicyBinding
import com.hashicorp.cdktf.providers.google.data_google_iam_policy.DataGoogleIamPolicyConfig
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKey
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKeyConfig
import com.hashicorp.cdktf.providers.google.kms_crypto_key_iam_policy.KmsCryptoKeyIamPolicy
import com.hashicorp.cdktf.providers.google.kms_crypto_key_iam_policy.KmsCryptoKeyIamPolicyConfig
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRing
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRingConfig
import com.hashicorp.cdktf.providers.google.provider.GoogleProvider
import com.hashicorp.cdktf.providers.google.service_account.ServiceAccount
import com.hashicorp.cdktf.providers.google.service_account.ServiceAccountConfig
import io.klogging.Klogging
import jarrid.keyper.app.CloudProviderConfig
import jarrid.keyper.resource.iam.RoleNotFoundException
import jarrid.keyper.resource.key.KeyNotFoundException
import jarrid.keyper.resource.key.Name
import jarrid.keyper.tfcdk.*
import software.constructs.Construct
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

class GCP(
    scope: Construct,
    stackName: String,
) : Klogging, Stack(scope, stackName = stackName) {

    override val provider: CloudProviderConfig
        get() {
            return config.provider.gcp
        }

    override suspend fun useCloudBackend() {
        logger.info("GCP provider credentials is set to ${provider.credentials}")
        GcsBackend(
            this, GcsBackendConfig.builder()
                .credentials(provider.credentials)
                .bucket(provider.backend.bucket)
                .prefix("terraform/state/$stackName")
                .build()
        )
    }

    override suspend fun useProvider() {
        logger.info("GCP provider credentials is set to ${provider.credentials}")
        GoogleProvider.Builder.create(this, "Google")
            .credentials(provider.credentials)
            .project(provider.accountId)
            .region(provider.region)
            .build()
    }

    private fun createServiceAccount(serviceAccount: ServiceAccountVar): ServiceAccount {
        return ServiceAccount(
            this, serviceAccount.name, ServiceAccountConfig.builder()
                .accountId(serviceAccount.accountId)
                .displayName(serviceAccount.name)
                .description(serviceAccount.description)
                .build()
        )
    }

    private fun createIamPolicy(policy: IamPolicyVar): CreateIamPolicyOutput {
        val saIamPolicy = DataGoogleIamPolicy(
            this, policy.saIamPolicyName, DataGoogleIamPolicyConfig.builder()
                .binding(
                    listOf(
                        DataGoogleIamPolicyBinding.builder()
                            .role("roles/cloudkms.cryptoKeyEncrypter")
                            .members(policy.members[KeyOperation.ENCRYPT]!!.map { "serviceAccount:${it.email}" })
                            .build(),
                        DataGoogleIamPolicyBinding.builder()
                            .role("roles/cloudkms.cryptoKeyDecrypter")
                            .members(policy.members[KeyOperation.DECRYPT]!!.map { "serviceAccount:${it.email}" })
                            .build()
                    )
                )
                .build()
        )
        val cryptoKeyIamPolicy = KmsCryptoKeyIamPolicy(
            this, policy.keyIamPolicyName, KmsCryptoKeyIamPolicyConfig.builder()
                .cryptoKeyId(policy.key.id)
                .policyData(saIamPolicy.policyData)
                .build()
        )
        return CreateIamPolicyOutput(saIamPolicy = saIamPolicy, cryptoKeyIamPolicy = cryptoKeyIamPolicy)
    }

    private fun createKmsKeyRing(keyRing: KmsKeyRingVar): KmsKeyRing {
        return KmsKeyRing(
            this, keyRing.keyRingName, KmsKeyRingConfig.builder()
                .name(keyRing.keyRingName)
                .location(provider.region)
                .build()
        )
    }

    private fun createSymmetricKey(key: KmsCryptoKeyVar, keyRing: KmsKeyRing): KmsCryptoKey {
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

    override fun createKeys(tfvar: DeploymentStack): CreateKeysOutput {
        val keyRingPayload = KmsKeyRingVar(
            deploymentId = tfvar.deployment.id,
            keyRingName = tfvar.deployment.name
        )
        val keyRing = createKmsKeyRing(keyRingPayload)
        val keys = tfvar.keys.associateWith { key ->
            val keyPayload = KmsCryptoKeyVar(
                keyName = Name.getJarridKeyName(key.base),
                keyId = key.base.id,
                ttl = key.ttl,
                rotationPeriod = key.rotationPeriod,
                labels = getLabels(key, deployment = tfvar.deployment)
            )
            createSymmetricKey(keyPayload, keyRing)
        }
        return GcpCreateKeysOutput(keyRing = keyRing, keys = keys)
    }

    override fun createRoles(tfvar: DeploymentStack): GcpCreateRolesOutput {
        val out = tfvar.roles.associateWith { role ->
            val name = validateRole(role, tfvar)
            val sa = ServiceAccountVar(
                name = name,
                accountId = Name.getSanitizedName(name),
                displayName = name,
                description = "jarrid-keyper service account. deployment-id: ${tfvar.deployment.base.id}"

            )
            createServiceAccount(sa)
        }
        return GcpCreateRolesOutput(roles = out)
    }


    private fun Map<Role, ServiceAccount>.getOrThrow(role: Role): ServiceAccount {
        return this[role] ?: throw RoleNotFoundException(role.base.name!!)
    }

    private fun Map<Key, KmsCryptoKey>.getOrThrow(key: Key): KmsCryptoKey {
        return this[key] ?: throw KeyNotFoundException(key.base.id.toString())
    }

    fun getIamPolicyVar(
        key: Key,
        keys: GcpCreateKeysOutput,
        roles: GcpCreateRolesOutput,
        tfvar: DeploymentStack,
    ): IamPolicyVar {

        val encrypt: List<ServiceAccount> = key.permission.allowEncrypt.map { role ->
            val useRole = getRole(role, tfvar)
            roles.roles.getOrThrow(useRole)
        }
        val decrypt: List<ServiceAccount> = key.permission.allowDecrypt.map { role ->
            val useRole = getRole(role, tfvar)
            roles.roles.getOrThrow(useRole)
        }
        return IamPolicyVar(
            saIamPolicyName = "jarrid-keyper-sa-policy-${key.base.id}",
            keyIamPolicyName = "jarrid-keyper-key-policy-${key.base.id}",
            members = mapOf(KeyOperation.ENCRYPT to encrypt, KeyOperation.DECRYPT to decrypt),
            key = keys.keys.getOrThrow(key)
        )
    }

    override fun createPermissions(
        tfvar: DeploymentStack,
        keys: CreateKeysOutput,
        roles: CreateRolesOutput
    ): CreatePermissionsOutput {
        val policies = tfvar.keys.map { key ->
            val iamPolicyVar = getIamPolicyVar(
                key,
                keys as GcpCreateKeysOutput,
                roles as GcpCreateRolesOutput,
                tfvar
            )
            createIamPolicy(iamPolicyVar)
        }
        return GcpCreatePermissionsOutput(policies)
    }
}