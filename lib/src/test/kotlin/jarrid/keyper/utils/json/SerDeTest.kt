package jarrid.keyper.utils.json


import jarrid.keyper.utils.model.NewUUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals

@Serializable
data class TestDataClass(
    val string: String,
    @Contextual val instant1: Instant? = null,
    @Contextual val instant2: Instant,
    @Contextual val any1: Any? = null,
    @Contextual val any2: Any,
    @Contextual val uuid1: UUID? = null,
    @Contextual val uuid2: UUID
)

class SerDeTest {
    @Test
    fun testSerialization() {
        val serde = SerDe()
        val testData = TestDataClass(
            string = "string",
            instant2 = Instant.now(),
            any2 = false,
            uuid2 = NewUUID.get()
        )
        val encoded: String = serde.json.encodeToString(
            TestDataClass.serializer(),
            testData
        )
        val decoded: TestDataClass = serde.json.decodeFromString(encoded)
        assertEquals(testData, decoded)
    }
}