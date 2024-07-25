package jarrid.keyper.tfcdk

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jarrid.keyper.resource.*
import jarrid.keyper.utils.file.Backend
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import com.hashicorp.cdktf.App as CdktfApp
import jarrid.keyper.resource.App as ResourceApp
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key
import jarrid.keyper.tfcdk.Stack as TfStack

@ExtendWith(MockKExtension::class)
class TfAppTest {

    @MockK
    lateinit var backend: Backend

    @MockK
    lateinit var tfStack: TfStack

    private lateinit var tfApp: TfApp

    private lateinit var appConfig: ResourceApp

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        // Create mock objects for appConfig
        val tfcdkConfig = Tfcdk(stack = Stack.GCP)

        // Mock ResourceBackend and ProviderConfig
        val resourceBackend = mockk<ResourceBackend>()
        val providerConfig = mockk<ProviderConfig>()

        // Set expectations for mock objects
        every { resourceBackend.backend.backend.get() } returns backend
        every { providerConfig.tfcdk } returns tfcdkConfig

        // Initialize appConfig with mock objects
        appConfig = ResourceApp(
            outDir = "/root",
            resource = resourceBackend,
            provider = providerConfig
        )

        // Initialize the tfApp with the mocked appConfig
        tfApp = spyk(TfApp(appConfig))
    }

    @Test
    fun testGetAppConfig() = runBlocking {
        val actual = tfApp.getAppConfig()
        assertEquals("/root/cdktf.out", actual.outdir)
    }

    @Test
    fun testCreateAppWithValidPrimaryConstructor() = runBlocking {
        val deployment = Deployment.create(BasePayload(name = "test-deployment"))
        val deploymentStack = DeploymentStack(
            deployment,
            listOf(Key.create(BasePayload(name = "test-key"))),
            listOf(Role.create(BasePayload(name = "test-role")))
        )
        val deployments = listOf(deploymentStack)
        coEvery { backend.getDeploymentStack() } returns deployments

        // Mock the getTfApp method using coAnswers
        val cdktfApp = mockk<CdktfApp>()
        coEvery { tfApp.getTfApp() } coAnswers { cdktfApp }

        // Mock the getTfStack method
        coEvery { tfApp.getTfStack(any(), any(), any()) } returns tfStack

        // Mock the tfStack.create(deployment) method
        coEvery { tfStack.create(any<DeploymentStack>()) } just Runs

        // Mock the synth method on the mockCdktfApp
        every { cdktfApp.synth() } just Runs

        tfApp.create()

        deployments.forEach { coVerify { tfStack.create(it) } }
        verify { cdktfApp.synth() }
    }
}