package jarrid.keyper.api

import io.klogging.Klogging
import jarrid.keyper.key.ManagerImpl
import jarrid.keyper.key.Payload
import java.util.*

class KeyServiceImpl(options: Options) : Klogging, Service(options) {
    suspend fun createSymmetricKey(context: Map<String, Any>? = null): UUID {
        val mgr = ManagerImpl(Payload(context = context), backend)
        val config = mgr.createKey()
        logger.info("Created key config: $config")
        return config.keyId!!
    }

    suspend fun encrypt(keyId: UUID, plaintext: String): String {
        TODO()
    }
}
