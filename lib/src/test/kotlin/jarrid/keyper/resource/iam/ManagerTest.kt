package jarrid.keyper.resource.iam

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Payload
import jarrid.keyper.resource.Stack
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.json.SerDe
import jarrid.keyper.utils.model.NewTimestamp
import jarrid.keyper.utils.model.NewUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockKExtension::class)
class IAMManagerTest {

    @MockK
    lateinit var backend: Backend

    @MockK
    lateinit var stack: Stack

    private lateinit var manager: Manager
    private val serde = SerDe()

    data class CreateRoleTestCase(
        val payload: Payload,
        val expected: Model? = null,
        val exception: KClass<out Throwable>? = null
    )

    companion object {
        val deploymentId = NewUUID.get()
        val resourceId = NewUUID.get()
        val created = NewTimestamp.get()
        private val context = mapOf("role" to "value")
        private val deployment = Deployment.new(
            id = deploymentId,
            name = "test-deployment",
            context = context
        )
        private val role = Model.create(
            BasePayload(
                id = resourceId,
                name = "test-role",
                context = context
            )
        ).apply {
            base.created = created
        }

        @JvmStatic
        fun createRoleProvider(): List<CreateRoleTestCase> {
            return listOf(
                CreateRoleTestCase(
                    payload = Payload(
                        deployment = BasePayload(
                            id = deploymentId,
                            name = deployment.name,
                            context = context
                        ),
                        resource = BasePayload(
                            id = resourceId,
                            name = role.name,
                            context = context
                        )
                    ),
                    expected = role
                ),
                CreateRoleTestCase(
                    payload = Payload(
                        deployment = BasePayload(
                            id = deploymentId,
                            name = deployment.name,
                            context = context
                        ),
                        resource = BasePayload(
                            id = resourceId,
                            context = context
                        )
                    ),
                    exception = RoleNameIsUndefinedException::class
                )
            )
        }
    }

    @BeforeEach
    fun setup() {
        mockkStatic(NewUUID::class)
        every { NewUUID.get() } returnsMany listOf(deploymentId, resourceId)
        mockkStatic(NewTimestamp::class)
        every { NewTimestamp.get() } returns created

        manager = Manager(backend, stack)
    }

    @ParameterizedTest
    @MethodSource("createRoleProvider")
    fun testCreateRole(case: CreateRoleTestCase) {
        runBlocking {
            coEvery { backend.getDeployment(any()) } returns deployment
            coEvery { backend.write(any<Model>(), any<Deployment>()) } just Runs

            if (case.exception != null) {
                assertFailsWith(case.exception) {
                    runBlocking {
                        manager.createRole(case.payload)
                    }
                }
            } else {
                val actual = manager.createRole(case.payload)
                assertEquals(serde.encode(case.expected), serde.encode(actual))
                coVerify { backend.write(actual, deployment) }
            }
        }
    }
}