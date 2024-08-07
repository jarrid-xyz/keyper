package jarrid.keyper.resource.key.data

import com.google.cloud.kms.v1.CryptoKeyName
import io.klogging.Klogging
import jarrid.keyper.app.Backend
import jarrid.keyper.app.Config
import jarrid.keyper.app.Stack
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.key.Name

abstract class Base(
    val backend: Backend,
    val stack: Stack,
    val key: Model,
) : Klogging {

    private val app = Config().get()
    val provider = stack.getConfig(app)
    private val projectId = provider.accountId
    private val region = provider.region

    fun getKeyName(key: Model): CryptoKeyName {
        return CryptoKeyName.of(
            projectId,
            region,
            key.deployment.name,
            Name.getJarridKeyName(key.resource.base)
        )
    }
}