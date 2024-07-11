package jarrid.keyper.cli.key

import jarrid.keyper.cli.KeySubcommand
import kotlinx.coroutines.runBlocking

class List(help: String = "List existing keys") : KeySubcommand(help = help) {
    override val usage = null
    override fun run() = runBlocking {
        val useBackend = backend.get()
        val deployments = useBackend.getDeploymentStacks()
        for (deployment in deployments) {
            echo("DeploymentId: ${deployment.deploymentId}")
            for (key in deployment.keys) {
                echo("Key: ${deployment.keys}")
            }
        }
    }
}