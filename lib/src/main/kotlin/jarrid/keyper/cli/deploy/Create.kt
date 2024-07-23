package jarrid.keyper.cli.deploy

import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.option
import jarrid.keyper.cli.DeploySubcommand
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Manager
import java.util.*

class Create : DeploySubcommand(help = "Create a deployment") {
    val name: String? by option(
        help = "Deployment name"
    )
    val id: UUID? by option(
        help = "Deployment id"
    ).convert { UUID.fromString(it) }

    val context: Map<String, String> by option(
        "-c", "--context",
        help = "Provide additional context as key:value map"
    ).associate()

    override suspend fun runAsync() {
        val manager = Manager(backend.get(), stack)
        val payload = BasePayload(
            id = id,
            name = name,
            context = context
        )

        manager.createDeployment(payload)
    }
}