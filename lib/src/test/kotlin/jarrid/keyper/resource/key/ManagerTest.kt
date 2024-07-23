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
        val expected: Key,
        val expectException: Boolean = false
    )

    companion object {
        val deploymentId = NewUUID.get()
        val resourceId = NewUUID.get()
        val created = NewTimestamp.get()
        private val context = mapOf("key" to "value")
        private val deployment = Deployment.new(
            id = deploymentId,
            name = "test-deployment",
            context = context
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
                            name = key.name,
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
                    expected = key,
                    expectException = true
                )
            )
        }
    }

    @BeforeEach
    fun setup() {
        mockkStatic(NewUUID::class)
        every { NewUUID.get() } returns resourceId
        mockkStatic(NewTimestamp::class)
        every { NewTimestamp.get() } returns created

        manager = Manager(backend, stack)
    }

    @ParameterizedTest
    @MethodSource("createKeyProvider")
    fun testCreateKey(case: CreateKeyTestCase) {
        runBlocking {
            coEvery { backend.getDeployment(any()) } returns deployment
            coEvery { backend.write(any<Key>(), any<Deployment>()) } just Runs

            if (case.expectException) {
                assertFailsWith<ResourceIsUndefinedException> {
                    manager.createKey(case.payload)
                }
            } else {
                val actual = manager.createKey(case.payload)
                assertEquals(serde.encode(case.expected), serde.encode(actual))
                coVerify { backend.write(actual, deployment) }
            }
        }
    }
}