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
import jarrid.keyper.resource.iam.Model as Role

@ExtendWith(MockKExtension::class)
class ManagerTest {

    @MockK
    lateinit var backend: Backend

    @MockK
    lateinit var stack: Stack

    private lateinit var manager: Manager
    private val serde = SerDe()

    data class CreateRoleTestCase(
        val payload: Payload,
        val expected: Role? = null,
        val exception: KClass<out Throwable>? = null
    )

    data class ListTestCase<Role>(
        val payload: Payload,
        val expected: List<Role>
    )

    companion object {
        val deploymentId = NewUUID.get()
        val resourceId = NewUUID.get()
        val created = NewTimestamp.get()
        private val context = mapOf("role" to "value")
        private val deployment = Deployment.create(
            BasePayload(
                id = deploymentId,
                name = "test-deployment",
                context = context
            )
        )
        private val role = Role.create(
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
                            name = role.base.name,
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

        @JvmStatic
        fun listRoleProvider(): List<ListTestCase<Role>> {
            return listOf(
                ListTestCase(
                    payload = Payload(
                        deployment = BasePayload(
                            id = deploymentId,
                            name = deployment.name,
                            context = context
                        )
                    ),
                    expected = listOf(role)
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
            coEvery { backend.getDeployment(any<Deployment>()) } returns deployment
            coEvery { backend.write(any<Role>(), any<Deployment>()) } just Runs

            if (case.exception != null) {
                assertFailsWith(case.exception) {
                    manager.createRole(case.payload)
                }
            } else {
                every { NewUUID.get() } returnsMany listOf(resourceId, deploymentId)
                val actual = manager.createRole(case.payload)
                if (case.expected != null) {
                    assertEquals(serde.encode(case.expected.base), serde.encode(actual.base))
                }
                coVerify { backend.write(actual, deployment) }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("listRoleProvider")
    fun testListRoles(case: ListTestCase<Role>) {
        coEvery { backend.getDeployment(any<Deployment>()) } returns deployment
        coEvery { backend.getResources<Role>(any()) } returns case.expected
        val actual = runBlocking { manager.list(case.payload) }
        assertEquals(case.expected.map { it.base }, actual.map { it.base })
        coVerify { backend.getResources<Role>(any()) }
    }
}