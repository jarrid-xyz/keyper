package jarrid.keyper.resource

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
open class Model(
    @Contextual val resource: Resource,
    @Contextual val deployment: Deployment,
) {
    companion object {
        fun get(resource: Resource, deployment: Deployment): Model {
            return Model(
                resource = resource,
                deployment = deployment
            )
        }
    }
}
