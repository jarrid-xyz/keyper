package jarrid.keyper.cli.resource

import jarrid.keyper.cli.ResourceSubcommand
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Manager
import jarrid.keyper.resource.Payload
import jarrid.keyper.resource.ResourceType
import jarrid.keyper.resource.iam.Manager as IAMManager
import jarrid.keyper.resource.key.Manager as KeyManager

class Create(help: String = "Create resource by resource types") : ResourceSubcommand(help = help) {
    override suspend fun runAsync() {
        val deployment = BasePayload(
            name = deployment,
        )
        val payload = Payload(
            deployment = deployment,
            resource = BasePayload(
                name = resourceName,
                context = context
            )
        )

        when (resourceType) {
            ResourceType.KEY -> {
                val manager = KeyManager(backend.get(), stack)
                manager.createKey(payload)
            }

            ResourceType.ROLE -> {
                val manager = IAMManager(backend.get(), stack)
                manager.createRole(payload)
            }

            ResourceType.DEPLOYMENT -> {
                val manager = Manager(backend.get(), stack)
                manager.createDeployment(deployment)
            }
        }
    }
}