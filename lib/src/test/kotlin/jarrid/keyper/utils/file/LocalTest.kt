package jarrid.keyper.utils.file

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import io.mockk.*
import jarrid.keyper.app.Config
import jarrid.keyper.key.Model
import jarrid.keyper.key.Usage
import jarrid.keyper.utils.json.encode
import jarrid.keyper.utils.model.NewTimestamp
import jarrid.keyper.utils.model.NewUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.TestInstance.Lifecycle
import java.nio.file.FileSystem
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

@TestInstance(Lifecycle.PER_CLASS)
class LocalTest {

    private lateinit var local: Local
    private lateinit var fileSystem: FileSystem
    private lateinit var rootDir: Path
    private val deploymentId = NewUUID.get()
    private val keyId = NewUUID.get()
    private val keyConfig = Model(
        deploymentId = deploymentId,
        keyId = keyId,
        created = NewTimestamp.get(),
        usage = Usage.CREATE_KEY
    )
    private var prefixDirPath: String = ""
    private var configFilePath = ""

    private fun getNewKeyConfig(): Model {
        return Model(
            deploymentId = NewUUID.get(),
            keyId = NewUUID.get(),
            created = NewTimestamp.get(),
            usage = Usage.CREATE_KEY
        )
    }

    @BeforeEach
    fun setUp() {
        // Create an in-memory file system with read-write permissions
        fileSystem = Jimfs.newFileSystem(Configuration.unix())
        rootDir = fileSystem.getPath("/mock/root")
        Files.createDirectories(rootDir)

        // Mock Config to return the in-memory file system rootDir
        val configMock = spyk(Config())
        every { configMock.outDir } returns rootDir

        // Initialize the Local instance with the mocked Config
        local = spyk(Local(configMock))

        // Set up necessary mocks and stubs
        coEvery { local.rootDir } returns rootDir

        // Initialize paths for the test
        prefixDirPath = local.getPrefix(keyConfig)
        configFilePath = local.getFileName(keyConfig)
    }

    @Test
    fun testWrite() = runBlocking {
        // Call the write method
        local.write(keyConfig)

        // Verify that the createDir and writeFile methods were called
        coVerify { local.createDir(keyConfig) }
        coVerify { local.writeFile(keyConfig) }

        // Check if the directory is created correctly in the in-memory file system
        val dirPath = rootDir.resolve(prefixDirPath)
        assertTrue(Files.exists(dirPath) && Files.isDirectory(dirPath))

        // Check if the file is written correctly in the in-memory file system
        val filePath = rootDir.resolve(configFilePath)
        assertTrue(Files.exists(filePath))
        val content = Files.readString(filePath)
        assertEquals(encode(keyConfig), content)
    }

    data class GetOrCreateDeploymentIdTestCase(
        val byDeploymentId: UUID? = null,
        val keyConfigs: List<Model>,
        val force: Boolean,
        val expectedDeploymentId: UUID?
    )

    @Nested
    inner class TestsWithStaticMocks {

        @BeforeEach
        fun setUpMocks() {
            mockkStatic(NewUUID::class)
            mockkStatic(NewTimestamp::class)
        }

        @AfterEach
        fun tearDownMocks() {
            unmockkAll()
        }

        @Test
        fun testGetOrCreateDeploymentId() = runBlocking {
            val keyConfig1 = getNewKeyConfig()
            val keyConfig2 = getNewKeyConfig()
            every { NewUUID.get() } returnsMany listOf(deploymentId)
            val cases = listOf(
                GetOrCreateDeploymentIdTestCase(
                    keyConfigs = emptyList(),
                    force = false,
                    expectedDeploymentId = null
                ),
                GetOrCreateDeploymentIdTestCase(
                    keyConfigs = emptyList(),
                    force = true,
                    expectedDeploymentId = deploymentId
                ),
                GetOrCreateDeploymentIdTestCase(
                    keyConfigs = listOf(
                        keyConfig1,
                    ),
                    force = false,
                    expectedDeploymentId = keyConfig1.deploymentId
                ),
                GetOrCreateDeploymentIdTestCase(
                    keyConfigs = listOf(
                        keyConfig1,
                    ),
                    force = true,
                    expectedDeploymentId = keyConfig1.deploymentId
                ),
                GetOrCreateDeploymentIdTestCase(
                    keyConfigs = listOf(
                        keyConfig1,
                        keyConfig2,
                    ),
                    force = false,
                    expectedDeploymentId = null
                ),
                GetOrCreateDeploymentIdTestCase(
                    byDeploymentId = NewUUID.get(),
                    keyConfigs = emptyList(),
                    force = true,
                    expectedDeploymentId = deploymentId
                )
            )

            for (case in cases) {
                for (keyConfig in case.keyConfigs) {
                    local.write(keyConfig)
                }
                val actual = local.getOrCreateDeploymentId(byDeploymentId = case.byDeploymentId, force = case.force)
                assertEquals(case.expectedDeploymentId, actual)
            }
        }
    }

    @Test
    fun testGetConfigs() = runBlocking {
        local.write(keyConfig)

        // Call the getConfigs method
        val configs = local.getConfigs()

        // Verify that the configs are returned correctly
        assertEquals(1, configs.size)
        assertEquals(keyConfig, configs[0])
    }

    data class GetConfigByIdTestCase(
        val byDeploymentId: UUID? = null,
        val runWrite: Boolean = false,
        val keyId: UUID,
        val expected: Model? = null,
    )

    @Test
    fun testGetConfigById() = runBlocking {
        val cases = listOf(
            GetConfigByIdTestCase(
                keyId = NewUUID.get(),
            ),
            GetConfigByIdTestCase(
                runWrite = true,
                keyId = NewUUID.get(),
            ),
            GetConfigByIdTestCase(
                runWrite = true,
                keyId = keyId,
                expected = keyConfig,
            ),
            GetConfigByIdTestCase(
                runWrite = true,
                byDeploymentId = deploymentId,
                keyId = keyId,
                expected = keyConfig,
            ),
            GetConfigByIdTestCase(
                runWrite = true,
                byDeploymentId = NewUUID.get(),
                keyId = keyId,
            ),
            GetConfigByIdTestCase(
                runWrite = true,
                byDeploymentId = deploymentId,
                keyId = NewUUID.get(),
            ),
        )
        for (case in cases) {
            if (case.runWrite) {
                local.write(keyConfig)
            }
            val actual = local.getConfig(byDeploymentId = case.byDeploymentId, keyId = case.keyId)
            assertEquals(case.expected, actual)
        }
    }

    companion object {
        @JvmStatic
        @AfterAll
        fun tearDownClass(): Unit {
            unmockkAll()
        }

        @JvmStatic
        @BeforeAll
        fun setUpClass(): Unit {
            mockkStatic(NewUUID::class)
            mockkStatic(NewTimestamp::class)
        }
    }
}