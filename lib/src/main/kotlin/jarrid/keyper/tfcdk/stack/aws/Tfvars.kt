package jarrid.keyper.tfcdk.stack.aws

import com.hashicorp.cdktf.providers.aws.iam_policy.IamPolicy
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

data class KeyPermissions(
    val allowEncrypt: MutableList<Key> = mutableListOf(),
    val allowDecrypt: MutableList<Key> = mutableListOf()
)

data class IamPolicyVars(
    val roleToKeyPermissions: Map<Role, KeyPermissions>
)

data class IamPolicyOutput(
    val out: Map<Role, IamPolicy>
)