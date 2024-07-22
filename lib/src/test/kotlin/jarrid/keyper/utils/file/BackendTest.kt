package jarrid.keyper.utils.file

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jarrid.keyper.resource.*
import jarrid.keyper.tfcdk.DeploymentStack
import jarrid.keyper.utils.json.SerDe
import jarrid.keyper.utils.model.NewUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key


@ExtendWith(MockKExtension::class)
class BackendTest {

    @MockK
    lateinit var config: Config
    private lateinit var backend: Local
    private val serde = SerDe()

    @BeforeEach
    fun setup() {
        mockkStatic(NewUUID::class)
        every { NewUUID.get() } returnsMany listOf(deploymentId, resourceId)

        every { config.get() } returns App(
            outDir = "root",
            manager = File(
                file = BackendConfig(
                    path = "dir"
                )
            )
        )

        backend = spyk(Local(config))
        Files.createDirectories(Paths.get("root/dir"))
    }

    data class CreateDeploymentDirTestCase(
        val deployment: Deployment?,
        val exists: Boolean,
        val create: Boolean
    )

    data class WriteTestCase(
        val deployment: Deployment,
        val resource: Resource? = null,
        val expectedEncoded: String,
        val expectedPath: String,
    )

    data class GetDeploymentTestCase(
        val deployment: Deployment?,
        val getDeployments: List<Deployment>,
        val error: KClass<out Throwable>? = null,
        val expected: Deployment? = null
    )

    data class GetDeploymentsTestCase(
        val folders: List<String>,
        val folderExists: Boolean = true,
        val deploymentExists: Boolean = true,
        val deploymentFileName: String = "",
        val expected: List<Deployment> = emptyList()
    )

    data class GetResourceTestCase(
        val resource: Resource,
        val json: String = "",
        val error: Boolean = false
    )

    data class GetResourcesTestCase(
        val deployment: Deployment,
        val files: List<String>,
        val type: ResourceType,
        val deploymentExists: Boolean = true,
        val expected: List<Resource> = emptyList()
    )

    data class GetDeploymentStackTestCase(
        val deployments: List<Deployment>,
        val keys: List<Key>,
        val roles: List<Role>,
        val expected: List<DeploymentStack>
    )

    companion object {
        val deploymentId = NewUUID.get()
        private val resourceId = NewUUID.get()
        val deployment = Deployment(
            _id = deploymentId,
            _name = "default",
            _context = null
        )

        @JvmStatic
        fun createDeploymentDirProvider(): List<CreateDeploymentDirTestCase> = listOf(
            CreateDeploymentDirTestCase(
                deployment = null,
                exists = false,
                create = true
            ), // deployment is null and should be created
            CreateDeploymentDirTestCase(
                deployment = null,
                exists = true,
                create = false
            ), // deployment is null and already exists
            CreateDeploymentDirTestCase(
                Deployment(
                    _id = deploymentId,
                    _name = "default",
                    _context = null
                ),
                exists = false,
                create = true
            ), // deployment exists and should be created
            CreateDeploymentDirTestCase(
                Deployment(
                    _id = deploymentId,
                    _name = "default",
                    _context = null
                ),
                exists = true,
                create = false
            ) // deployment does not exist and should not be created
        )

        @JvmStatic
        fun writeProvider(): List<WriteTestCase> {
            val deployment = Deployment(
                _id = deploymentId,
                _name = "default",
                _context = null
            )
            return listOf(
                WriteTestCase(
                    deployment = deployment,
                    expectedPath = "root/dir/$deploymentId/deployment.json",
                    expectedEncoded = """{"base":{"created":null,"updated":null,"id":"$deploymentId","name":"default","context":null},"type":"DEPLOYMENT"}"""
                ),
                WriteTestCase(
                    deployment = deployment,
                    resource = Key(
                        id = resourceId,
                    ),
                    expectedPath = "root/dir/$deploymentId/key/$resourceId.json",
                    expectedEncoded = """{"base":{"created":null,"updated":null,"id":"$resourceId","name":null,"context":null},"type":"KEY"}"""
                )
            )
        }

        @JvmStatic
        fun getDeploymentProvider(): List<GetDeploymentTestCase> = listOf(
            GetDeploymentTestCase(
                deployment = null,
                getDeployments = listOf(
                    Deployment(
                        _id = deploymentId,
                        _name = "default",
                        _context = null
                    )
                ),
                expected = Deployment(
                    _id = deploymentId,
                    _name = "default",
                    _context = null
                )
            ), // deployment is null, find the first one
            GetDeploymentTestCase(
                deployment = Deployment(
                    _id = NewUUID.get(),
                    _name = "non-existent",
                    _context = null
                ),
                getDeployments = listOf(
                    Deployment(
                        _id = deploymentId,
                        _name = "default",
                        _context = null
                    )
                ),
                error = DeploymentNotFoundException::class,
                expected = null
            ), // deployment is defined but couldn't match either id or name
            GetDeploymentTestCase(
                deployment = Deployment(
                    _id = deploymentId,
                    _name = "default",
                    _context = null
                ),
                getDeployments = listOf(
                    Deployment(
                        _id = deploymentId,
                        _name = "default",
                        _context = null
                    )
                ),
                expected = Deployment(
                    _id = deploymentId,
                    _name = "default",
                    _context = null
                )
            ), // deployment is defined and found match with id or name
            GetDeploymentTestCase(
                deployment = null,
                getDeployments = listOf(
                    Deployment(
                        _id = deploymentId,
                        _name = "default",
                        _context = null
                    ),
                    Deployment(
                        _id = NewUUID.get(),
                        _name = "other",
                        _context = null
                    )
                ),
                error = MultipleDeploymentsFoundException::class,
                expected = null
            ) // multiple deployments found, should throw an error
        )

        @JvmStatic
        fun getDeploymentsProvider(): List<GetDeploymentsTestCase> = listOf(
            GetDeploymentsTestCase(
                folders = emptyList(),
                folderExists = false,
                expected = emptyList()
            ), // ls(dir) throws DirectoryNotFoundException dir doesn't exist
            GetDeploymentsTestCase(
                folders = listOf(deploymentId.toString()),
                deploymentExists = false,
                deploymentFileName = "root/dir/$deploymentId/deployment.json",
                expected = emptyList()
            ), // read deployment.json file in folder and doesn't exist, throws DeploymentNotFoundException
            GetDeploymentsTestCase(
                folders = listOf(deploymentId.toString()),
                folderExists = true,
                deploymentFileName = "root/dir/$deploymentId/deployment.json",
                expected = listOf(
                    Deployment(
                        _id = deploymentId,
                        _name = "default",
                        _context = null
                    )
                )
            ), // read deployment.json successful, return and verify
        )

        @JvmStatic
        fun getResourceProvider(): List<GetResourceTestCase> = listOf(
            GetResourceTestCase(
                resource = Key(id = resourceId),
                json = """{"type":"KEY","base":{"id":"$resourceId","name":null,"created":null,"updated":null,"context":null}}"""
            ),
            GetResourceTestCase(
                resource = Key(id = resourceId),
                error = true
            )
        )

        @JvmStatic
        fun getResourcesProvider(): List<GetResourcesTestCase> = listOf(
            // Deployment doesn't exist
            GetResourcesTestCase(
                deployment = Deployment(
                    _id = NewUUID.get(),
                    _name = "default",
                    _context = null
                ),
                files = emptyList(),
                type = ResourceType.KEY,
                deploymentExists = false
            ),
            // Files are empty
            GetResourcesTestCase(
                deployment = deployment,
                files = listOf("deployment.json"),
                type = ResourceType.KEY,
                expected = emptyList()
            ),
            // Files contain only deployment.json
            GetResourcesTestCase(
                deployment = deployment,
                files = listOf("deployment.json"),
                type = ResourceType.KEY,
                expected = emptyList()
            ),
            // Files contain valid UUID
            GetResourcesTestCase(
                deployment = deployment,
                files = listOf("deployment.json", "$resourceId.json"),
                type = ResourceType.KEY,
                expected = listOf(
                    Resource(
                        base = Base(id = resourceId, name = null),
                        type = ResourceType.KEY
                    )
                )
            )
        )

        @JvmStatic
        fun getDeploymentStackProvider(): List<GetDeploymentStackTestCase> {
            val deployment = Deployment(
                _id = deploymentId,
                _name = "default",
                _context = null
            )
            val key = Key(
                id = resourceId,
                ttl = 7
            )
            val role = Role(
                id = resourceId,
                name = "admin"
            )
            return listOf(
                GetDeploymentStackTestCase(
                    deployments = listOf(deployment),
                    keys = listOf(key),
                    roles = listOf(role),
                    expected = listOf(
                        DeploymentStack(
                            deployment = deployment,
                            keys = listOf(key),
                            roles = listOf(role)
                        )
                    )
                ),
                GetDeploymentStackTestCase(
                    deployments = emptyList(),
                    keys = emptyList(),
                    roles = emptyList(),
                    expected = emptyList()
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("createDeploymentDirProvider")
    fun testCreateDeploymentDir(case: CreateDeploymentDirTestCase) = runBlocking {
        // Use the provided deployment or create a new one with the deploymentId
        val useDeployment = case.deployment ?: Deployment(
            _id = deploymentId,
            _name = "default",
            _context = null
        )
        val path = Backend.joinPaths("root", "dir", useDeployment.id.toString())
        // Mock the exists method
        every { backend.exists(path) } returns case.exists
        backend.createDeploymentDir(case.deployment)
        if (case.create) {
            verify { backend.createDir(path) }
        } else {
            verify(exactly = 0) { backend.createDir(path) }
        }
    }

    @ParameterizedTest
    @MethodSource("writeProvider")
    fun testWrite(case: WriteTestCase) = runBlocking {
        coEvery { backend.write(any<String>(), any<String>()) } just Runs
        if (case.resource == null) {
            backend.write(case.deployment)
        } else {
            backend.write(case.resource, case.deployment)
        }

        // Verify that the write method was called with the correct arguments
        coVerify { backend.write(case.expectedPath, case.expectedEncoded) }
    }

    @ParameterizedTest
    @MethodSource("getDeploymentProvider")
    fun testGetDeployment(case: GetDeploymentTestCase) {
        // Mock the getDeployments method to return the provided list
        coEvery { backend.getDeployments() } returns case.getDeployments

        if (case.error != null) {
            assertFailsWith(case.error) {
                backend.getDeployment(case.deployment)
            }
        } else {
            val actual = backend.getDeployment(case.deployment)
            assertEquals(actual, case.expected)
        }
    }

    @ParameterizedTest
    @MethodSource("getDeploymentsProvider")
    fun testGetDeployments(case: GetDeploymentsTestCase) {
        if (!case.folderExists) {
            coEvery { backend.ls(any()) } throws DirectoryNotFoundException("")
            assertFailsWith<DirectoryNotFoundException> { backend.getDeployments() }
        } else {
            coEvery { backend.ls(any()) } returns case.folders

            if (!case.deploymentExists) {
                coEvery { backend.read(case.deploymentFileName) } throws DeploymentNotFoundException("")
                assertFailsWith<DeploymentNotFoundException> { backend.getDeployments() }
            } else {
                coEvery { backend.read(case.deploymentFileName) } returns """{"type":"DEPLOYMENT","base":{"id":"$deploymentId","name":"default"}}"""
                val actual = backend.getDeployments()
                assertListsEqual(case.expected, actual)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getResourceProvider")
    fun testGetResource(case: GetResourceTestCase) {
        coEvery { backend.getDeployments() } returns listOf(deployment)
        if (case.error) {
            coEvery { backend.read(any()) } throws ResourceNotFoundException("Resource not found")
            assertFailsWith<ResourceNotFoundException> {
                backend.getResource(case.resource)
            }
        } else {
            coEvery { backend.read(any()) } returns case.json
            val actual = backend.getResource(case.resource)
            val encoded = serde.encode(actual)
            assertEquals(encoded, serde.encode(case.resource))
        }
    }

    @ParameterizedTest
    @MethodSource("getResourcesProvider")
    fun testGetResources(case: GetResourcesTestCase) {
        runBlocking {
            if (!case.deploymentExists) {
                coEvery { backend.getDeployments() } returns emptyList()
                assertFailsWith<DeploymentNotFoundException> {
                    backend.getResources(case.deployment, case.type)
                }
            } else {
                coEvery { backend.getDeployments() } returns listOf(case.deployment)
                coEvery { backend.ls(any()) } returns case.files

                if (case.files.isNotEmpty() && case.files.contains("$resourceId.json")) {
                    coEvery { backend.read(any()) } returns serde.encode(
                        Resource(
                            base = Base(id = resourceId, name = null),
                            type = ResourceType.KEY
                        )
                    )
                }

                val result = backend.getResources(case.deployment, case.type)
                assertListsEqual(case.expected, result)
            }
        }
    }

    @ParameterizedTest
    @MethodSource("getDeploymentStackProvider")
    fun testGetDeploymentStack(case: GetDeploymentStackTestCase) = runBlocking {
        coEvery { backend.getDeployments() } returns case.deployments
        for (deployment in case.deployments) {
            coEvery { backend.getResources(deployment, ResourceType.KEY) } returns case.keys
            coEvery { backend.getResources(deployment, ResourceType.ROLE) } returns case.roles
        }

        val result = backend.getDeploymentStack()
        assertListsEqual(case.expected, result)
    }

    private inline fun <reified T> assertListsEqual(expected: List<T>, actual: List<T>) {
        assertEquals(expected.size, actual.size, "${T::class.simpleName} list sizes are not equal")
        expected.zip(actual).forEach { (expectedItem, actualItem) ->
            val expectedJson = serde.encode(expectedItem)
            val actualJson = serde.encode(actualItem)
            assertEquals(expectedJson, actualJson, "${T::class.simpleName}s are not equal")
        }
    }
}