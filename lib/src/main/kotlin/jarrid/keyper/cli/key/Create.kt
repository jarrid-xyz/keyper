package jarrid.keyper.cli.key

import com.github.ajalt.clikt.parameters.options.option
import com.google.gson.annotations.SerializedName
import jarrid.keyper.cli.KeySubcommand
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Payload
import jarrid.keyper.resource.key.Manager

enum class Action {
    @SerializedName("Create AES Key")
    CREATE_AES_KEY,

    @SerializedName("List")
    LIST
}

class Create(help: String = "Create key") : KeySubcommand(help = help) {
    override val action: Action = Action.CREATE_AES_KEY
    val name: String? by option(
        help = "Key name"
    )

    override suspend fun runAsync() {
        val payload = Payload(
            deployment = BasePayload(
                id = deploymentId,
                name = deploymentName,
            ),
            resource = BasePayload(
                name = name,
                context = context
            )
        )
        val manager = Manager(backend.get(), stack)
        manager.createKey(payload)
    }
}