package jarrid.keyper.utils.file

import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import jarrid.keyper.resource.*
import jarrid.keyper.utils.json.SerDe
import jarrid.keyper.utils.model.NewTimestamp
import jarrid.keyper.utils.model.NewUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.test.assertEquals

@ExtendWith(MockKExtension::class)
class Backend {

    @MockK
    lateinit var config: Config
    private lateinit var backend: Local

    private val deploymentId = NewUUID.get()
    private val resourceId = NewUUID.get()
    private val created = NewTimestamp.get()

    @BeforeEach
    fun setup() {
        every { config.get() } returns App(
            outDir = "root",
            manager = File(
                file = BackendConfig(
                    path = "dir"
                )
            )
        )

        backend = spyk(Local(config))
        Files.createDirectories(Paths.get("root/dir"))
    }

    @Test
    fun testCreateDir() = runBlocking {
        val deployment = Deployment(id = deploymentId)
        backend.createDir(deployment)

        val path = Paths.get("root/dir", deployment.id.toString())
        assert(Files.exists(path))
    }

    @Test
    fun testWriteDeployment() = runBlocking {
        val deployment = Deployment(id = deploymentId)
        backend.write(deployment)

        val path = Paths.get("root/dir", deployment.id.toString(), "deployment.json")
        assert(Files.exists(path))
        assertEquals(SerDe.encode(deployment), Files.readString(path))
    }

    @Test
    fun testWriteResource() = runBlocking {
        val deployment = Deployment(id = deploymentId)
        val resource = Resource(
            resourceType = ResourceType.KEY,
            id=resourceId,
            name = "key"
        )
        val model = Model(
            resource = resource,
            base = Base(
                created = created,
                deployment = deployment,
                context = mapOf()
            )
        )

        backend.write(model)

        val path = Paths.get("root/dir", deployment.id.toString(), "${resource.id}.json")
        assert(Files.exists(path))
        assertEquals(SerDe.encode(model), Files.readString(path))
    }

    @Test
    fun testGetDeployments() = runBlocking {
        val deployment = Deployment(id = deploymentId)
        backend.write(deployment)

        val deployments = backend.getDeployments()
        assertEquals(1, deployments.size)
        assertEquals(deployment.id, deployments.first().id)
    }

    @Test
    fun testGetResource() = runBlocking {
        val deployment = Deployment(id = deploymentId)
        val resource = Resource(
            resourceType = ResourceType.KEY,
            id=resourceId
        )
        val model = Model(
            resource = resource,
            base = Base(
                created = created,
                deployment = deployment,
                context = mapOf()
            )
        )

        backend.write(model)

        val fetchedResource = backend.getResource(model)
        assertEquals(model, fetchedResource)
    }

//    @Test
//    fun testGetResourceThrowsKeyConfigNotFound() = runBlocking {
//        val deployment = Deployment(id = deploymentId)
//        val resource = Resource(
//            resourceType = ResourceType.KEY,
//            id = resourceId
//        )
//        val model = Model(
//            resource = resource,
//            base = Base(
//                created = Date().toInstant(),
//                deployment = deployment,
//                context = mapOf()
//            )
//        )
//
//        backend.getResource(model)
//    }
}