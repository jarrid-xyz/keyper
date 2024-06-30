package jarrid.keyper.key

import io.klogging.Klogging
import jarrid.keyper.utils.file.Backend

class ManagerImpl(
    private val payload: Payload,
    private val backend: Backend
) : Klogging, Manager {
    override suspend fun createKey(): Model {
        return Manager.run(payload, Usage.CREATE_KEY, backend)
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



