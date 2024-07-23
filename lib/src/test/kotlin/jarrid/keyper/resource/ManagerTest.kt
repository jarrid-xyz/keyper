package jarrid.keyper.resource

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.json.SerDe
import jarrid.keyper.utils.model.NewTimestamp
import jarrid.keyper.utils.model.NewUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class ManagerTest {

    @MockK
    lateinit var backend: Backend

    @MockK
    lateinit var stack: Stack

    private lateinit var manager: Manager
    private val serde = SerDe()

    data class CreateDeploymentTestCase(
        val payload: BasePayload,
        val expected: Deployment
    )

    data class ListTestCase(
        val payload: Payload,
        val expected: List<Deployment>
    )

    companion object {
        val deploymentId = NewUUID.get()
        val created = NewTimestamp.get()
        private val context = mapOf("key" to "value")
        private val deployment = Deployment.create(
            BasePayload(
                id = deploymentId,
                name = "test-deployment",
                context = context
            )
        )

        @JvmStatic
        fun createDeploymentProvider(): List<CreateDeploymentTestCase> {
            return listOf(
                CreateDeploymentTestCase(
                    payload = BasePayload(
                        id = deploymentId,
                        name = "test-deployment",
                        context = context
                    ),
                    expected = deployment.apply {
                        base.created = created
                    }
                ),
                CreateDeploymentTestCase(
                    payload = BasePayload(),
                    expected = Deployment.create(
                        BasePayload(
                            id = deploymentId,
                            name = "default",
                            context = null
                        )
                    ).apply {
                        base.created = created
                    }
                )
            )
        }

        @JvmStatic
        fun listDeploymentProvider(): List<ListTestCase> {
            return listOf(
                ListTestCase(
                    payload = Payload(
                        deployment = BasePayload(
                            id = deploymentId,
                            name = deployment.name,
                            context = context
                        )
                    ),
                    expected = listOf(deployment)
                )
            )
        }
    }

    @BeforeEach
    fun setup() {
        mockkStatic(NewUUID::class)
        every { NewUUID.get() } returns deploymentId
        mockkStatic(NewTimestamp::class)
        every { NewTimestamp.get() } returns created

        manager = Manager(backend, stack)
    }

    @ParameterizedTest
    @MethodSource("createDeploymentProvider")
    fun testCreateDeployment(case: CreateDeploymentTestCase) {
        runBlocking {
            coEvery { backend.createDeploymentDir(any()) } just Runs
            coEvery { backend.write(any<Deployment>()) } just Runs

            val actual = manager.createDeployment(case.payload)
            assertEquals(serde.encode(case.expected), serde.encode(actual))
            coVerify { backend.createDeploymentDir(actual) }
            coVerify { backend.write(actual) }
        }
    }

    @ParameterizedTest
    @MethodSource("listDeploymentProvider")
    fun testListDeployments(case: ListTestCase) {
        coEvery { backend.getDeployments() } returns case.expected
        val actual = manager.list()
        assertEquals(case.expected, actual)
        coVerify { backend.getDeployments() }
    }
}