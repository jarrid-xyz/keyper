package jarrid.keyper.tfcdk.gcp.stack

import com.hashicorp.cdktf.providers.google.data_google_iam_policy.DataGoogleIamPolicy
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKey
import com.hashicorp.cdktf.providers.google.kms_crypto_key_iam_policy.KmsCryptoKeyIamPolicy
import com.hashicorp.cdktf.providers.google.kms_key_ring.KmsKeyRing
import com.hashicorp.cdktf.providers.google.service_account.ServiceAccount
import java.util.*
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

data class KmsKeyRingVar(
    val deploymentId: UUID,
    val keyRingName: String,
)

data class KmsCryptoKeyVar(
    val keyName: String,
    val keyId: UUID,
    val ttl: Int,
    val rotationPeriod: String? = "7776000s",
    val labels: Map<String, String>
)

data class ServiceAccountVar(
    val name: String,
    val accountId: String,
    val displayName: String,
    val description: String,
)

data class CreateKeysOutput(
    val keyRing: KmsKeyRing,
    val keys: Map<Key, KmsCryptoKey>
)

data class CreateRolesOutput(
    val roles: Map<Role, ServiceAccount>
)

data class CreateIamPolicyOutput(
    val saIamPolicy: DataGoogleIamPolicy,
    val cryptoKeyIamPolicy: KmsCryptoKeyIamPolicy
)

data class CreatePermissionsOutput(
    val policies: List<CreateIamPolicyOutput>,
)

data class IamPolicyVar(
    val saIamPolicyName: String,
    val keyIamPolicyName: String,
    val members: Map<KeyOperation, List<ServiceAccount>>,
    val key: KmsCryptoKey
)

enum class KeyOperation {
    ENCRYPT,
    DECRYPT
}
