package jarrid.keyper.tfcdk.gcp.stack

import com.hashicorp.cdktf.LocalBackend
import com.hashicorp.cdktf.LocalBackendConfig
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
import jarrid.keyper.resource.CloudProviderConfig
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.iam.RoleNameIsUndefinedException
import jarrid.keyper.resource.key.Name
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.tfcdk.Stack
import software.constructs.Construct
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

class RoleNotFoundException(val name: String, message: String = "Role: $name not found") : Exception(message)
class KeyNotFoundException(val name: String, message: String = "Key: $name not found") : Exception(message)
class MultipleRolesFoundException(val name: String, message: String = "Multiple roles found by $name") :
    Exception(message)

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

    private fun createKeys(tfvar: DeploymentStack): CreateKeysOutput {
        val keyRingPayload = KmsKeyRingVar(
            deploymentId = tfvar.deployment.id,
            keyRingName = tfvar.deployment.name
        )
        val keyRing = createKmsKeyRing(keyRingPayload)
        val keys = tfvar.keys.associateWith { key ->
            val keyPayload = KmsCryptoKeyVar(
                keyName = key.base.name ?: Name.getJarridKeyName(key.base.id),
                keyId = key.base.id,
                ttl = key.ttl,
                rotationPeriod = key.rotationPeriod,
                labels = getLabels(key, deployment = tfvar.deployment)
            )
            createSymmetricKey(keyPayload, keyRing)
        }
        return CreateKeysOutput(keyRing = keyRing, keys = keys)
    }

    private fun validate(role: Role, tfvar: DeploymentStack): String {
        val name = role.base.name ?: throw RoleNameIsUndefinedException()
        getRole(name, tfvar)
        return name
    }

    private fun createRoles(tfvar: DeploymentStack): CreateRolesOutput {
        val out = tfvar.roles.associateWith { role ->
            val name = validate(role, tfvar)
            val sa = ServiceAccountVar(
                name = name,
                accountId = Name.getSanitizedAccountId(name),
                displayName = name,
                description = "jarrid-keyper service account. deployment-id: ${tfvar.deployment.base.id}"

            )
            createServiceAccount(sa)
        }
        return CreateRolesOutput(roles = out)
    }

    private fun getRole(name: String, tfvar: DeploymentStack): Role {
        val filtered = tfvar.roles.filter { role -> role.base.name == name }
        return when {
            filtered.isEmpty() -> throw RoleNotFoundException(name)
            filtered.size > 1 -> throw MultipleRolesFoundException(name)
            else -> filtered.first()
        }
    }

//    private fun getKeyToRoles(tfvar: DeploymentStack, type: KeyOperation): Map<Key, List<Role>> {
//        val allowed = mutableMapOf<Key, MutableList<Role>>()
//        tfvar.keys.forEach { key ->
//            val roles = when (type) {
//                KeyOperation.ENCRYPT -> key.permission.allowEncrypt
//                KeyOperation.DECRYPT -> key.permission.allowDecrypt
//                else -> throw IllegalArgumentException("Unknown permission type: $type")
//            }
//            roles.forEach { roleName ->
//                val role = getRole(roleName, tfvar)
//                allowed.computeIfAbsent(key) { mutableListOf() }.add(role)
//            }
//        }
//        return allowed
//    }

    private fun Map<Role, ServiceAccount>.getOrThrow(role: Role): ServiceAccount {
        return this[role] ?: throw RoleNotFoundException(role.base.name!!)
    }

    private fun Map<Key, KmsCryptoKey>.getOrThrow(key: Key): KmsCryptoKey {
        return this[key] ?: throw KeyNotFoundException(key.base.id.toString())
    }

    private fun getIamPolicyVar(
        key: Key,
        keys: CreateKeysOutput,
        roles: CreateRolesOutput,
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

    private fun createPermissions(
        tfvar: DeploymentStack,
        keys: CreateKeysOutput,
        roles: CreateRolesOutput
    ): CreatePermissionsOutput {
        val policies = tfvar.keys.map { key ->
            val iamPolicyVar = getIamPolicyVar(key, keys, roles, tfvar)
            createIamPolicy(iamPolicyVar)
        }
        return CreatePermissionsOutput(policies)
    }

    override suspend fun create(tfvar: DeploymentStack) {
        val createKeys = createKeys(tfvar)
        val createRoles = createRoles(tfvar)
        createPermissions(tfvar, createKeys, createRoles)
        logger.info("Created GCP terraform stack")
    }
}