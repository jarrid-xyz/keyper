package jarrid.keyper.key

import io.klogging.Klogging
import jarrid.keyper.utils.file.Backend

class ManagerImpl(
    private val payload: Payload,
    backend: Backend
) : Klogging, Manager(backend = backend) {
    override suspend fun createKey(): Model {
        return run(payload, Usage.CREATE_KEY)
    }

    override suspend fun shareKey() {
        TODO("Not yet implemented")
    }

    override suspend fun getKey() {
        TODO("Not yet implemented")
    }

    override suspend fun encrypt() {
        TODO("Not yet implemented")
    }

    override suspend fun decrypt() {
        TODO("Not yet implemented")
    }
}



