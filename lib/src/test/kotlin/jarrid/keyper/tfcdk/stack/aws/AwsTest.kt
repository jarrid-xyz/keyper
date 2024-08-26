package jarrid.keyper.tfcdk.stack.aws

import com.hashicorp.cdktf.Testing
import io.mockk.every
import io.mockk.mockk
import jarrid.keyper.resource.key.Permission
import jarrid.keyper.tfcdk.AwsCreateKeysOutput
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.utils.model.NewUUID
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import software.constructs.Construct
import kotlin.test.assertEquals
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

class AWSTest {

    private lateinit var aws: AWS
    private lateinit var scope: Construct

    @BeforeEach
    fun setUp() {
        scope = mockk<Construct>(relaxed = true)
        aws = AWS(Testing.app(), "test-stack")
    }

    data class TestCase(
        val roles: List<Role>,
        val keys: List<Key>,
        val expected: Map<Role, KeyPermissions>
    )

    companion object {
        private val key1 = Key(NewUUID.get(), permission = Permission(allowEncrypt = listOf("role1")))
        private val key2 = Key(NewUUID.get(), permission = Permission(allowDecrypt = listOf("role2")))
        private val key3 =
            Key(
                NewUUID.get(),
                permission = Permission(allowEncrypt = listOf("role1"), allowDecrypt = listOf("role2"))
            )
        private val role1 = Role(id = NewUUID.get(), name = "role1")
        private val role2 = Role(id = NewUUID.get(), name = "role2")

        @JvmStatic
        fun roleToKeyPermissionTestCases() = listOf(
            TestCase(
                roles = listOf(role1, role2),
                keys = listOf(key1, key2),
                expected = mapOf(
                    role1 to KeyPermissions(
                        allowEncrypt = mutableListOf(key1),
                        allowDecrypt = mutableListOf()
                    ),
                    role2 to KeyPermissions(
                        allowEncrypt = mutableListOf(),
                        allowDecrypt = mutableListOf(key2)
                    )
                )
            ),
            TestCase(
                roles = listOf(role1, role2),
                keys = listOf(key1, key2, key3),
                expected = mapOf(
                    role1 to KeyPermissions(
                        allowEncrypt = mutableListOf(key1, key3),
                        allowDecrypt = mutableListOf()
                    ),
                    role2 to KeyPermissions(
                        allowEncrypt = mutableListOf(),
                        allowDecrypt = mutableListOf(key2, key3)
                    )
                )
            )
        )
    }

    @ParameterizedTest
    @MethodSource("roleToKeyPermissionTestCases")
    fun testMapRoleToKeyPermissions(testCase: TestCase) {
        // Mock the DeploymentStack with the roles and keys from the test case
        val deployment = mockk<DeploymentStack>(relaxed = true)
        every { deployment.roles } returns testCase.roles
        every { deployment.keys } returns testCase.keys

        // Create a map of keys to mock KmsKey objects
        val keysOutput = AwsCreateKeysOutput(
            keys = testCase.keys.associateWith { mockk(relaxed = true) }
        )

        // Call the function to test
        val result = aws.mapRoleToKeyPermissions(keysOutput, deployment)

        // Verify the output against the expected values
        assertEquals(testCase.expected, result)
    }
}