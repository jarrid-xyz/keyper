package jarrid.keyper.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.option
import jarrid.keyper.api.KeyServiceImpl
import jarrid.keyper.api.Options
import jarrid.keyper.key.Usage

abstract class KeySubcommand(help: String = "") :
    BaseSubcommand(help = help) {
    abstract val usage: Usage?
//    private val usage: Usage? by option(
//        help = "Specify usage"
//    ).enum<Usage>()

    val context: Map<String, String> by option(
        "-c", "--context",
        help = "Provide additional context as key:value map"
    ).associate()


    suspend fun runAsync() {
        val service = KeyServiceImpl(
            options = Options(
                stack = stack,
                backend = backend
            )
        )
        if (usage == null) {
            throw IllegalArgumentException("Must specify usage")
        }
        when (usage) {
            Usage.CREATE_KEY -> service.createSymmetricKey(context)
            else -> TODO()
        }
    }
}

class Key :
    NoOpCliktCommand(
        help = "Key module allow you to create, update, delete and list keys. For more information, visit: https://jarrid.xyz"
    )
