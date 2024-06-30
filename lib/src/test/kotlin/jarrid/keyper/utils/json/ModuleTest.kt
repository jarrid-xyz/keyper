package jarrid.keyper.utils.json


import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
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

class ModuleTest {
    @Test
    fun testSerialization() {

        val json = Json {
            serializersModule = module
        }
        val testData = TestDataClass(
            string = "string",
            instant2 = Instant.now(),
            any2 = false,
            uuid2 = UUID.randomUUID()
        )
        val encoded: String = json.encodeToString(
            TestDataClass.serializer(),
            testData
        )
        val decoded: TestDataClass = json.decodeFromString(encoded)
        assertEquals(testData, decoded)
    }
}