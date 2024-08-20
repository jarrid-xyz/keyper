package jarrid.keyper.tfcdk.stack.aws

import com.hashicorp.cdktf.S3Backend
import com.hashicorp.cdktf.S3BackendConfig
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocument
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentConfig
import com.hashicorp.cdktf.providers.aws.data_aws_iam_policy_document.DataAwsIamPolicyDocumentStatement
import com.hashicorp.cdktf.providers.aws.iam_policy.IamPolicy
import com.hashicorp.cdktf.providers.aws.iam_policy.IamPolicyConfig
import com.hashicorp.cdktf.providers.aws.iam_role.IamRole
import com.hashicorp.cdktf.providers.aws.iam_role.IamRoleConfig
import com.hashicorp.cdktf.providers.aws.kms_key.KmsKey
import com.hashicorp.cdktf.providers.aws.kms_key.KmsKeyConfig
import com.hashicorp.cdktf.providers.aws.provider.AwsProvider
import io.klogging.Klogging
import jarrid.keyper.app.CloudProviderConfig
import jarrid.keyper.resource.key.Name
import jarrid.keyper.tfcdk.*
import software.constructs.Construct
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

class AWS(
    scope: Construct,
    stackName: String,
) : Klogging, Stack(scope, stackName = stackName) {

    override val provider: CloudProviderConfig
        get() {
            return config.provider.gcp
        }

    override suspend fun useCloudBackend() {
        S3Backend(
            this, S3BackendConfig.builder()
                .region(provider.region)
                .bucket(provider.backend.bucket)
                .key("terraform/state/$stackName")
                .build()
        )
    }

    override suspend fun useProvider() {
        AwsProvider.Builder.create(this, "AWS")
            .region(provider.region)
            .build()
    }

    override fun createKeys(tfvar: DeploymentStack): AwsCreateKeysOutput {
        val keys = tfvar.keys.associateWith { key ->
            val keyConfig = KmsKeyConfig.builder()
                .keyUsage("ENCRYPT_DECRYPT")
                .enableKeyRotation(true)
                .rotationPeriodInDays(key.rotationPeriodDays)
                .description("Key for ${tfvar.deployment.name}")
                .tags(getLabels(key, deployment = tfvar.deployment))
                .build()

            val kmsKey = KmsKey(this, key.base.id.toString(), keyConfig)
            kmsKey
        }

        return AwsCreateKeysOutput(keys = keys)
    }

    private fun createIamRole(name: String, description: String): IamRole {
        return IamRole(
            this, name, IamRoleConfig.builder()
                .name(name)
                .description(description)
                .build()
        )
    }

    override fun createRoles(tfvar: DeploymentStack): AwsCreateRolesOutput {
        val out = tfvar.roles.associateWith { role ->
            val name = validate(role, tfvar)
            val description = "jarrid-keyper IAM role. deployment-id: ${tfvar.deployment.base.id}"
            createIamRole(name, description)
        }
        return AwsCreateRolesOutput(roles = out)
    }


    private fun mapRoleToKeyPermissions(
        keys: AwsCreateKeysOutput,
        tfvar: DeploymentStack
    ): Map<Role, KeyPermissions> {
        val map = mutableMapOf<Role, KeyPermissions>()

        // Helper function to add keys to the map for encryption or decryption
        fun addKeyToRoleMap(role: String, key: Key, permissionType: (KeyPermissions) -> MutableList<Key>) {
            val useRole = getRole(role, tfvar)
            val keyPermissions = map.getOrPut(useRole) { KeyPermissions() }
            permissionType(keyPermissions).add(key)
        }

        keys.keys.forEach { (key) ->
            key.permission.allowEncrypt.forEach { role ->
                addKeyToRoleMap(role, key) { it.allowEncrypt }
            }
            key.permission.allowDecrypt.forEach { role ->
                addKeyToRoleMap(role, key) { it.allowDecrypt }
            }
        }

        // Convert the mutable map to an immutable map
        return map
    }

    fun getIamPolicy(
        role: Role,
        statements: List<DataAwsIamPolicyDocumentStatement>,
        tfvar: DeploymentStack,
    ): IamPolicy {
        val name = validate(role, tfvar)
        val policy = DataAwsIamPolicyDocument(
            this,
            "jarrid-keyper KMS policy for role $name",
            DataAwsIamPolicyDocumentConfig.builder()
                .statement(statements)
                .build()
        )
        val out = IamPolicy(
            this, Name.getSanitizedAccountId(name), IamPolicyConfig.builder()
                .name(name)
                .description("jarrid-keyper role. deployment-id: ${tfvar.deployment.base.id}")
                .policy(policy.json)
                .build()
        )
        return out
    }

    private fun createIamPolicy(
        keys: AwsCreateKeysOutput,
        tfvar: DeploymentStack
    ): IamPolicyOutput {
        val map = mapRoleToKeyPermissions(keys, tfvar)

        // Helper function to create policy statements
        fun createPolicyStatement(
            sid: String,
            actions: List<String>,
            resources: List<String>,
        ): DataAwsIamPolicyDocumentStatement {
            return DataAwsIamPolicyDocumentStatement.builder()
                .effect("Allow")
                .actions(actions)
                .resources(resources)
                .sid(sid)
                .build()
        }


        val out = map.mapValues { (role, permissions) ->
            // Create the list of resources for the policy
            val encryptKeys = permissions.allowEncrypt.map { key -> keys.keys[key]!!.arn }
            val decryptKeys = permissions.allowDecrypt.map { key -> keys.keys[key]!!.arn }

            // Create the policy statements
            val statements = mutableListOf<DataAwsIamPolicyDocumentStatement>()

            if (encryptKeys.isNotEmpty()) {
                statements.add(
                    createPolicyStatement(
                        actions = listOf("kms:Encrypt", "kms:DescribeKey"),
                        resources = encryptKeys,
                        sid = "KMSKeyEncryptPermission"
                    )
                )
            }

            if (decryptKeys.isNotEmpty()) {
                statements.add(
                    createPolicyStatement(
                        actions = listOf("kms:Decrypt", "kms:DescribeKey"),
                        resources = decryptKeys,
                        sid = "KMSKeyDecryptPermission"
                    )
                )
            }
            getIamPolicy(role, statements, tfvar)
        }
        return IamPolicyOutput(out = out)
    }

    override fun createPermissions(
        tfvar: DeploymentStack,
        keys: CreateKeysOutput,
        roles: CreateRolesOutput
    ): CreatePermissionsOutput {
        // TODO: not sure if needed
        // createKeyPolicy(roles as AwsCreateRolesOutput, tfvar)
        val out = createIamPolicy(keys as AwsCreateKeysOutput, tfvar)
        return AwsCreatePermissionsOutput(out = out)
    }
}