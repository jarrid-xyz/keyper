package jarrid.keyper.tfcdk.stack

import io.mockk.every
import io.mockk.mockk
import jarrid.keyper.resource.iam.MultipleRolesFoundException
import jarrid.keyper.resource.iam.RoleNotFoundException
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.tfcdk.Stack
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import jarrid.keyper.resource.iam.Model as Role

class StackTest {

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
                DeploymentStack(mockk(), emptyList(), emptyList()),
                RoleNotFoundException::class.java
            ),
            // Test case for multiple roles found
            GetRoleTestCase(
                "test-role",
                DeploymentStack(
                    mockk(),
                    emptyList(),
                    listOf(mockk<Role> { every { base.name } returns "test-role" },
                        mockk<Role> { every { base.name } returns "test-role" })
                ),
                MultipleRolesFoundException::class.java
            ),
            // Test case for role found successfully
            GetRoleTestCase(
                "test-role",
                DeploymentStack(
                    mockk(),
                    emptyList(),
                    listOf(mockk<Role> { every { base.name } returns "test-role" })
                ),
            )
        )
    }

    @ParameterizedTest
    @MethodSource("roleTestCases")
    fun testGetRole(case: GetRoleTestCase) {
        if (case.expected != null) {
            assertThrows(case.expected) { Stack.getRole(case.name, case.tfvar) }
        } else {
            val result = Stack.getRole(case.name, case.tfvar)
            assertEquals(case.name, result.base.name)
        }
    }
}