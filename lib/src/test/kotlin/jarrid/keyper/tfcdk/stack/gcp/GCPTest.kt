package jarrid.keyper.tfcdk.stack.gcp

import com.hashicorp.cdktf.Testing
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKey
import com.hashicorp.cdktf.providers.google.service_account.ServiceAccount
import io.mockk.every
import io.mockk.mockk
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.key.EditPermission
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.tfcdk.GcpCreateKeysOutput
import jarrid.keyper.tfcdk.GcpCreatePermissionsOutput
import jarrid.keyper.tfcdk.GcpCreateRolesOutput
import jarrid.keyper.utils.model.NewUUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import software.constructs.Construct
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key


class GCPTest {

    private lateinit var gcp: GCP
    private lateinit var scope: Construct
    private val keyId = NewUUID.get()
    private val role = "test-role"
    private val email = "service-account@example.com"

    @BeforeEach
    fun setUp() {
        scope = mockk<Construct>(relaxed = true)
        gcp = GCP(Testing.app(), "test-stack")
    }

    @Test
    fun testGetIamPolicyVar() {
        val key = Key(id = keyId)

        key.editPermission(EditPermission.ADD_ALLOW_ENCRYPT, listOf(role))
        key.editPermission(EditPermission.ADD_ALLOW_DECRYPT, listOf(role))

        val serviceAccount = mockk<ServiceAccount>(relaxed = true)
        val kmsCryptoKey = mockk<KmsCryptoKey>(relaxed = true)

        every { serviceAccount.email } returns email

        val keysOutput = GcpCreateKeysOutput(mockk(relaxed = true), mapOf(key to kmsCryptoKey))
        val rolesOutput = GcpCreateRolesOutput(mapOf(Role(name = role) to serviceAccount))
        val tfvar = DeploymentStack(Deployment(), listOf(key), listOf(Role(name = role)))
        val result = gcp.getIamPolicyVar(key, keysOutput, rolesOutput, tfvar)

        assertEquals("jarrid-keyper-sa-policy-$keyId", result.saIamPolicyName)
        assertEquals("jarrid-keyper-key-policy-$keyId", result.keyIamPolicyName)
        assertEquals(kmsCryptoKey, result.key)
        assertEquals(1, result.members[KeyOperation.ENCRYPT]!!.size)
        assertEquals(1, result.members[KeyOperation.DECRYPT]!!.size)
        assertEquals(email, result.members[KeyOperation.ENCRYPT]!![0].email)
    }

    @Test
    fun testCreatePermissions() {
        val key = Key(id = keyId)
        val serviceAccount = mockk<ServiceAccount>(relaxed = true)
        val kmsCryptoKey = mockk<KmsCryptoKey>(relaxed = true)

        every { serviceAccount.email } returns "service-account@example.com"

        val keysOutput = GcpCreateKeysOutput(mockk(relaxed = true), mapOf(key to kmsCryptoKey))
        val rolesOutput = GcpCreateRolesOutput(mapOf(Role(name = "test-role") to serviceAccount))
        val tfvar = DeploymentStack(Deployment(), listOf(key), listOf(Role(name = "test-role")))
        val createPermissionsOutput = gcp.createPermissions(tfvar, keysOutput, rolesOutput)

        assertEquals(1, (createPermissionsOutput as GcpCreatePermissionsOutput).policies.size)
    }
}