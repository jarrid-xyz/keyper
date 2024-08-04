package jarrid.keyper.cli.resource

import io.mockk.*
import jarrid.keyper.cli.Helper
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.ResourceType
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.io.OutputStream
import java.io.PrintStream
import kotlin.test.assertEquals
import jarrid.keyper.resource.Manager as ResourceManager
import jarrid.keyper.resource.iam.Manager as IAMManager
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Manager as KeyManager
import jarrid.keyper.resource.key.Model as Key


class ListResourceTest {

    private lateinit var keyManager: KeyManager
    private lateinit var iamManager: IAMManager
    private lateinit var manager: ResourceManager
    private lateinit var listResource: ListResource

    private val keys: List<Key> = listOf(
        Key(name = "key1"),
        Key(name = "key2"),
    )

    private val roles: List<Role> = listOf(
        Role(name = "role1"),
        Role(name = "role2")
    )

    private val deployments: List<Deployment> = listOf(
        Deployment(_name = "d1"),
        Deployment(_name = "d2")
    )

    @BeforeEach
    fun setUp() {
        keyManager = mockk()
        iamManager = mockk()
        manager = mockk()
        listResource = spyk(ListResource())
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    data class ListCommandTestCase(
        val args: Array<String>,
        val resourceType: ResourceType,
        val expectedOutput: String
    )

    companion object {
        @JvmStatic
        fun listResourceTestCases(): List<ListCommandTestCase> {
            return listOf(
                ListCommandTestCase(
                    args = arrayOf("--resource-type", "KEY"),
                    resourceType = ResourceType.KEY,
                    expectedOutput = "Keys:\nkey1\nkey2"
                ),
                ListCommandTestCase(
                    args = arrayOf("--resource-type", "ROLE"),
                    resourceType = ResourceType.ROLE,
                    expectedOutput = "Roles:\nrole1\nrole2"
                ),
                ListCommandTestCase(
                    args = arrayOf("--resource-type", "DEPLOYMENT"),
                    resourceType = ResourceType.DEPLOYMENT,
                    expectedOutput = "Deployments:\nd1\nd2"
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("listResourceTestCases")
    fun testListResource(case: ListCommandTestCase) = runBlocking {
        println("Running test with args: ${case.args.joinToString(" ")}")


        // Mock the method to return the correct resource list based on the resource type
        when (case.resourceType) {
            ResourceType.KEY -> {
                every { listResource.getManager<KeyManager>() } returns keyManager
                coEvery { keyManager.list(any()) } returns keys
            }

            ResourceType.ROLE -> {
                every { listResource.getManager<IAMManager>() } returns iamManager
                coEvery { iamManager.list(any()) } returns roles
            }

            ResourceType.DEPLOYMENT -> {
                every { listResource.getManager<ResourceManager>() } returns manager
                coEvery { manager.list() } returns deployments
            }
        }

        // Ensure the command arguments are correctly set up
        Helper.parseCommand(listResource, case.args)

        // Capture the output
        val outputStream = mockk<OutputStream>(relaxed = true)
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))

        // Run the command
        listResource.runAsync()

        // Verify the output
        val output = Helper.captureStdOutSuspend {
            listResource.runAsync()
        }
        assertEquals(case.expectedOutput, output)

        // Verify the command was called as expected
        when (case.resourceType) {
            ResourceType.KEY -> {
                coVerify { keyManager.list(any()) }
            }

            ResourceType.ROLE -> {
                coVerify { iamManager.list(any()) }
            }

            ResourceType.DEPLOYMENT -> {
                coVerify { manager.list() }
            }
        }

        // Restore the original System.out
        System.setOut(originalOut)
    }

}