package jarrid.keyper.tfcdk.gcp.stack

import com.hashicorp.cdktf.Testing
import com.hashicorp.cdktf.providers.google.kms_crypto_key.KmsCryptoKey
import com.hashicorp.cdktf.providers.google.service_account.ServiceAccount
import io.mockk.every
import io.mockk.mockk
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.key.EditPermission
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.utils.model.NewUUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
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

    data class GetRoleTestCase(
        val name: String,
        val tfvar: DeploymentStack,
        val expected: Class<out Throwable>? = null
    )

    companion object {
        @JvmStatic
        fun roleTestCases() = listOf(
            // Test case for role not found
            GetRoleTestCase(
                "non-existent-role",
                DeploymentStack(Deployment(), emptyList(), emptyList()),
                RoleNotFoundException::class.java
            ),
            // Test case for multiple roles found
            GetRoleTestCase(
                "test-role",
                DeploymentStack(
                    Deployment(),
                    emptyList(),
                    listOf(Role(name = "test-role"), Role(name = "test-role"))
                ),
                MultipleRolesFoundException::class.java
            ),
            // Test case for role found successfully
            GetRoleTestCase(
                "test-role",
                DeploymentStack(
                    Deployment(),
                    emptyList(),
                    listOf(Role(name = "test-role"))
                ),
            )
        )
    }

    @ParameterizedTest
    @MethodSource("roleTestCases")
    fun testGetRole(case: GetRoleTestCase) {
        if (case.expected != null) {
            assertThrows(case.expected) { gcp.getRole(case.name, case.tfvar) }
        } else {
            val result = gcp.getRole(case.name, case.tfvar)
            assertEquals(case.name, result.base.name)
        }
    }

    @Test
    fun testGetIamPolicyVar() {
        val key = Key(id = keyId)

        key.editPermission(EditPermission.ADD_ALLOW_ENCRYPT, listOf(role))
        key.editPermission(EditPermission.ADD_ALLOW_DECRYPT, listOf(role))

        val serviceAccount = mockk<ServiceAccount>(relaxed = true)
        val kmsCryptoKey = mockk<KmsCryptoKey>(relaxed = true)

        every { serviceAccount.email } returns email

        val keysOutput = CreateKeysOutput(mockk(relaxed = true), mapOf(key to kmsCryptoKey))
        val rolesOutput = CreateRolesOutput(mapOf(Role(name = role) to serviceAccount))
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

        val keysOutput = CreateKeysOutput(mockk(relaxed = true), mapOf(key to kmsCryptoKey))
        val rolesOutput = CreateRolesOutput(mapOf(Role(name = "test-role") to serviceAccount))
        val tfvar = DeploymentStack(Deployment(), listOf(key), listOf(Role(name = "test-role")))
        val createPermissionsOutput = gcp.createPermissions(tfvar, keysOutput, rolesOutput)

        assertEquals(1, createPermissionsOutput.policies.size)
    }
}