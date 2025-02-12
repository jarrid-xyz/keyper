package jarrid.keyper.cli

import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import jarrid.keyper.resource.ResourceType
import jarrid.keyper.resource.key.Manager
import kotlinx.coroutines.runBlocking

abstract class ResourceSubcommand(help: String = "", name: String? = null) :
    BaseSubcommand(help, name) {

    val deployment: String? by option(
        "-d", "--deployment", help = "Deployment name"
    ).default("default")

    val resourceName: String? by option(
        "-n", "--resource-name",
        help = "Resource name"
    )

    val resourceType: ResourceType by option(
        "-t", "--resource-type", help = "Resource type"
    ).enum<ResourceType>().required()

    val context: Map<String, String> by option(
        "-c", "--context", help = "Provide additional context as key:value map"
    ).associate()

    @Suppress("UNCHECKED_CAST")
    fun <T> getManager(): T {
        return when (resourceType) {
            ResourceType.KEY -> Manager(backend.get(), stack) as T
            ResourceType.ROLE -> jarrid.keyper.resource.iam.Manager(backend.get(), stack) as T
            ResourceType.DEPLOYMENT -> jarrid.keyper.resource.Manager(backend.get(), stack) as T
        }
    }

    override fun run() {
        runBlocking {
            logger.info(
                "Running Resource command with the following options: " +
                        "Backend: $backend, Stack: $stack, Context: $context, ResourceType: $resourceType"
            )
            runAsync()
        }
    }

    abstract suspend fun runAsync()
}

class Resource(
    help: String =
        "Resource module allows you to create, update, delete and list resources. " +
                "For more information, visit: https://jarrid.xyz/keyper"
) :
    Keyper(help)