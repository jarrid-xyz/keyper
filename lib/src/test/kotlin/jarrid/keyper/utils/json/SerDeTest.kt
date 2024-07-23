package jarrid.keyper.utils.json


import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Deployment
import jarrid.keyper.utils.model.NewUUID
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.time.Instant
import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import jarrid.keyper.resource.iam.Model as Role
import jarrid.keyper.resource.key.Model as Key

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

    @Test
    fun testKeySerialization() {
        val serde = SerDe()
        val key = Key.create(BasePayload())
        val encoded: String = serde.encode(key)
        val decoded: Key = serde.decode(encoded)
        assertEquals(serde.encode(key.base), serde.encode(decoded.base))
    }

    @Test
    fun testRoleSerialization() {
        val serde = SerDe()
        val role = Role.create(BasePayload(name = "test"))
        val encoded: String = serde.encode(role)
        val decoded: Role = serde.decode(encoded)
        assertEquals(serde.encode(role.base), serde.encode(decoded.base))
    }

    @Test
    fun testDeploymentSerialization() {
        val serde = SerDe()
        val deployment = Deployment.create(BasePayload())
        val encoded: String = serde.encode(deployment)
        val decoded: Deployment = serde.decode(encoded)
        assertEquals(serde.encode(deployment.base), serde.encode(decoded.base))
    }
}