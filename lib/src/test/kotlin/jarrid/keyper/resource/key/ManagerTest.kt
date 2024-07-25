package jarrid.keyper.resource.key

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jarrid.keyper.resource.*
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
import jarrid.keyper.resource.key.Model as Key

@ExtendWith(MockKExtension::class)
class ManagerTest {

    @MockK
    lateinit var backend: Backend

    @MockK
    lateinit var stack: Stack

    private lateinit var manager: Manager
    private val serde = SerDe()

    data class CreateKeyTestCase(
        val payload: Payload,
        val expected: Key? = null,
        val exception: KClass<out Throwable>? = null
    )

    data class ListTestCase<Key>(
        val payload: Payload,
        val expected: List<Key>
    )

    data class PermissionTestCase(
        val type: EditPermission,
        val roles: List<String>,
        val started: Permission? = null,
        val expected: Permission
    )

    companion object {
        private val deploymentId = NewUUID.get()
        private val resourceId = NewUUID.get()
        private val created = NewTimestamp.get()
        private val context = mapOf("key" to "value")
        private val deployment = Deployment.create(
            BasePayload(
                id = deploymentId,
                name = "test-deployment",
                context = context
            )
        )
        private val key = Key(
            id = resourceId,
            name = "test-key",
            ttl = 7
        ).apply {
            base.created = created
        }

        @JvmStatic
        fun createKeyProvider(): List<CreateKeyTestCase> {
            return listOf(
                CreateKeyTestCase(
                    payload = Payload(
                        deployment = BasePayload(
                            id = deploymentId,
                            name = deployment.name,
                            context = context
                        ),
                        resource = BasePayload(
                            id = resourceId,
                            name = key.base.name,
                            context = mapOf("ttl" to 7)
                        )
                    ),
                    expected = key
                ),
                CreateKeyTestCase(
                    payload = Payload(
                        deployment = BasePayload(),
                        resource = BasePayload(
                            name = "default-key",
                            context = mapOf("ttl" to 7)
                        )
                    ),
                    expected = Key(
                        id = resourceId,
                        name = "default-key",
                        ttl = 7
                    ).apply {
                        base.created = created
                    }
                ),
                CreateKeyTestCase(
                    payload = Payload(
                        deployment = BasePayload(
                            id = deploymentId,
                            name = deployment.name,
                            context = context
                        ),
                        resource = null
                    ),
                    exception = ResourceIsUndefinedException::class
                )
            )
        }

        @JvmStatic
        fun listKeyProvider(): List<ListTestCase<Key>> {
            return listOf(
                ListTestCase(
                    payload = Payload(
                        deployment = BasePayload(
                            id = deploymentId,
                            name = deployment.name,
                            context = context
                        )
                    ),
                    expected = listOf(key)
                )
            )
        }

        @JvmStatic
        fun permissionProvider(): List<PermissionTestCase> = listOf(
            PermissionTestCase(
                type = EditPermission.ADD_ALLOW_ENCRYPT,
                roles = listOf("role1"),
                expected = Permission(allowEncrypt = listOf("role1"), allowDecrypt = emptyList())
            ),
            PermissionTestCase(
                type = EditPermission.ADD_ALLOW_DECRYPT,
                roles = listOf("role2", "role2"),
                expected = Permission(allowEncrypt = emptyList(), allowDecrypt = listOf("role2"))
            ),
            PermissionTestCase(
                type = EditPermission.REMOVE_ALLOW_ENCRYPT,
                roles = listOf("role1"),
                started = Permission(allowEncrypt = listOf("role1"), allowDecrypt = emptyList()),
                expected = Permission(allowEncrypt = emptyList(), allowDecrypt = emptyList())
            ),
            PermissionTestCase(
                type = EditPermission.REMOVE_ALLOW_DECRYPT,
                roles = listOf("role1"),
                started = Permission(allowEncrypt = emptyList(), allowDecrypt = listOf("role2")),
                expected = Permission(allowEncrypt = emptyList(), allowDecrypt = listOf("role2"))
            ),
            PermissionTestCase(
                type = EditPermission.REMOVE_ALLOW_DECRYPT,
                roles = listOf("role2"),
                expected = Permission(allowEncrypt = emptyList(), allowDecrypt = emptyList())
            )
        )
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
    @MethodSource("createKeyProvider")
    fun testCreateKey(case: CreateKeyTestCase) {
        runBlocking {
            coEvery { backend.getDeployment(any()) } returns deployment
            coEvery { backend.write(any<Key>(), any()) } just Runs

            if (case.exception != null) {
                assertFailsWith(case.exception) {
                    manager.createKey(case.payload)
                }
            } else {
                // not sure why, the order is backwards (?)
                every { NewUUID.get() } returnsMany listOf(resourceId, deploymentId)
                val actual = manager.createKey(case.payload)
                if (case.expected != null) {
                    assertEquals(serde.encode(case.expected.base), serde.encode(actual.base))
                }
                coVerify { backend.write(actual, deployment) }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("listKeyProvider")
    fun testListKeys(case: ListTestCase<Key>) {
        coEvery { backend.getDeployment(any<Deployment>()) } returns deployment
        coEvery { backend.getResources<Key>(any()) } returns case.expected
        val actual = runBlocking { manager.list(case.payload) }
        assertEquals(case.expected.map { it.base }, actual.map { it.base })
        coVerify { backend.getResources<Key>(any()) }
    }

    @ParameterizedTest
    @MethodSource("permissionProvider")
    fun testPermission(case: PermissionTestCase) = runBlocking {
        val payload = Payload(resource = BasePayload(id = resourceId, name = "test-key"), deployment = null)
        val resource = Resource(base = Base(id = resourceId, name = "test-key"))
        val key: Key = Key(id = resourceId, name = "test-key").apply {
            if (case.started != null) {
                permission = case.started
            }
        }

        // Set up mocks
        every { manager.getOrCreateDeployment(payload) } returns deployment
        every { backend.getResourceWithCheck(payload, ResourceType.KEY) } returns resource
        every { backend.getResource<Key>(resource, deployment) } returns key
        coEvery { backend.write(any<Key>(), any()) } just Runs

        // Call the permission method
        manager.permission(payload, case.type, case.roles)

        // Verify the permission change
        assertEquals(case.expected, key.permission)
    }
}