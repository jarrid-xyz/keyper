package jarrid.keyper.cli.resource

import jarrid.keyper.cli.ResourceSubcommand
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Manager
import jarrid.keyper.resource.Payload
import jarrid.keyper.resource.ResourceType
import jarrid.keyper.resource.iam.Manager as IAMManager
import jarrid.keyper.resource.key.Manager as KeyManager

class List(help: String = "List  resources") : ResourceSubcommand(help = help) {
    override suspend fun runAsync() {
        val deployment = BasePayload(
            name = deployment,
        )
        val payload = Payload(
            deployment = deployment
        )

        when (resourceType) {
            ResourceType.KEY -> {
                val manager = KeyManager(backend.get(), stack)
                val resources = manager.list(payload)
                echo("Keys: ${resources.joinToString { it.base.name ?: it.base.id.toString() }}")
            }

            ResourceType.ROLE -> {
                val manager = IAMManager(backend.get(), stack)
                val resources = manager.list(payload)
                echo("Roles: ${resources.joinToString { it.base.name!! }}")
            }

            ResourceType.DEPLOYMENT -> {
                val manager = Manager(backend.get(), stack)
                val resources = manager.list()
                echo("Deployments: ${resources.joinToString { it.name }}")
            }
        }
    }
}