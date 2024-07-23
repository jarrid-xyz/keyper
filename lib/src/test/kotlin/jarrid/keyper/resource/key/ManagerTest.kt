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

    companion object {
        val deploymentId = NewUUID.get()
        val resourceId = NewUUID.get()
        val created = NewTimestamp.get()
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
        val keyManager = Manager(backend, stack)
        coEvery { backend.getDeployment(any<Deployment>()) } returns deployment
        coEvery { backend.getResources<Key>(any()) } returns case.expected
        val actual = runBlocking { keyManager.list(case.payload) }
        assertEquals(case.expected.map { it.base }, actual.map { it.base })
        coVerify { backend.getResources<Key>(any()) }
    }
}