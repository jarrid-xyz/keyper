package jarrid.keyper.key

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import jarrid.keyper.utils.file.Local
import jarrid.keyper.utils.model.NewTimestamp
import jarrid.keyper.utils.model.NewUUID
import kotlinx.coroutines.test.runTest
import java.time.Clock
import java.util.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals


class ManagerImplTest {

    @BeforeTest
    fun setUp() {
        mockkStatic(NewUUID::class)
        mockkStatic(NewTimestamp::class)
    }

    @AfterTest
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun testConvert() = runTest {
        val keyId = UUID.randomUUID()
        val deploymentId = UUID.randomUUID()
        every { NewUUID.get() } returnsMany listOf(keyId, deploymentId)
        val now = Clock.systemUTC().instant()
        every { NewTimestamp.get() } returns now

        val cases = listOf(
            mapOf<String, Any>(
                "payload" to Payload(),
                "usage" to Usage.CREATE_KEY,
                "expected" to Model(
                    usage = Usage.CREATE_KEY,
                    keyId = keyId,
                    created = now,
                    deploymentId = deploymentId
                )
            )
        )
        for (case in cases) {
            val payload = case["payload"] as Payload
            val usage = case["usage"] as Usage
            val expected = case["expected"] as Model
            val backend = Local()
            val actual = Manager.convert(payload, usage, backend)
            assertEquals(expected, actual)
        }
    }
}