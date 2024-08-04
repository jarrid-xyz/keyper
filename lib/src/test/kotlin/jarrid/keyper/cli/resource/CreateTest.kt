package jarrid.keyper.cli.resource

import io.mockk.*
import jarrid.keyper.cli.Helper
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Manager
import jarrid.keyper.resource.ResourceType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import jarrid.keyper.resource.Manager as ResourceManager
import jarrid.keyper.resource.iam.Manager as IAMManager
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Manager as KeyManager
import jarrid.keyper.resource.key.Model as Key

class CreateResourceTest {

    private lateinit var keyManager: KeyManager
    private lateinit var iamManager: IAMManager
    private lateinit var manager: ResourceManager
    private lateinit var create: Create

    @BeforeEach
    fun setUp() {
        keyManager = mockk()
        iamManager = mockk()
        manager = mockk()
        create = spyk(Create())
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    data class CommandTestCase(
        val args: Array<String>,
        val resourceType: ResourceType,
        val expected: String
    )

    companion object {
        @JvmStatic
        fun createResourceTestCases(): List<CommandTestCase> {
            return listOf(
                CommandTestCase(
                    args = arrayOf("--resource-type", "KEY", "--resource-name", "testKey"),
                    resourceType = ResourceType.KEY,
                    expected = "Key created successfully"
                ),
                CommandTestCase(
                    args = arrayOf("--resource-type", "ROLE", "--resource-name", "testRole"),
                    resourceType = ResourceType.ROLE,
                    expected = "Role created successfully"
                ),
                CommandTestCase(
                    args = arrayOf("--resource-type", "DEPLOYMENT", "--resource-name", "testDeployment"),
                    resourceType = ResourceType.DEPLOYMENT,
                    expected = "Deployment created successfully"
                )
            )
        }
    }


    @ParameterizedTest
    @MethodSource("createResourceTestCases")
    fun testCreateResource(case: CommandTestCase) = runBlocking {
        // Mock the backend.get() method based on the resource type
        when (case.resourceType) {
            ResourceType.KEY -> {
                every { create.getManager<KeyManager>() } returns keyManager
                coEvery { keyManager.createKey(any()) } returns Key()
            }

            ResourceType.ROLE -> {
                every { create.getManager<IAMManager>() } returns iamManager
                coEvery { iamManager.createRole(any()) } returns Role()
            }

            ResourceType.DEPLOYMENT -> {
                every { create.getManager<Manager>() } returns manager
                coEvery { manager.createDeployment(any()) } returns Deployment()
            }
        }

        // Ensure the command arguments are correctly set up
        Helper.parseCommand(create, case.args)

        // Run the command
        create.runAsync()

        // Verify the command was called as expected
        when (case.resourceType) {
            ResourceType.KEY -> {
                coVerify { keyManager.createKey(any()) }
            }

            ResourceType.ROLE -> {
                coVerify { iamManager.createRole(any()) }
            }

            ResourceType.DEPLOYMENT -> {
                coVerify { manager.createDeployment(any()) }
            }
        }
    }
}