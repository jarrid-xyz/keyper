package jarrid.keyper.cli.resource

import jarrid.keyper.cli.ResourceSubcommand
import jarrid.keyper.resource.*
import jarrid.keyper.resource.iam.Manager as IAMManager
import jarrid.keyper.resource.key.Manager as KeyManager

class ListResource(help: String = "List resources by resource types", name: String? = "list") :
    ResourceSubcommand(help = help, name = name) {

    fun show(resources: List<Resource>) {
        resources.forEach {
            echo(it.base.name ?: it.base.id)
        }
    }

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
                echo("Keys:")
                show(resources)

            }

            ResourceType.ROLE -> {
                val manager = IAMManager(backend.get(), stack)
                val resources = manager.list(payload)
                echo("Roles:")
                show(resources)
            }

            ResourceType.DEPLOYMENT -> {
                val manager = Manager(backend.get(), stack)
                val resources = manager.list()
                echo("Deployments:")
                show(resources)
            }
        }
    }
}