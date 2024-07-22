package jarrid.keyper.resource.key.data

import io.klogging.Klogging
import jarrid.keyper.resource.*

abstract class Base(
    val backend: Backend,
    val stack: Stack,
    val key: Model,
) : Klogging {

    private val app = Config().get()
    private val provider = stack.getConfig(app)!!
    val useBackend = backend.get()
    val projectId = provider.accountId
    val region = provider.region


    suspend fun getKeyResource(): Resource {
        val deployment = useBackend.getDeployment()
        val key = useBackend.getResource(
            deployment,
        )
        return key
    }
}