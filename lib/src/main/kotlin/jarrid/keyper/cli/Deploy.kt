package jarrid.keyper.cli

import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.multiple
import com.github.ajalt.clikt.parameters.arguments.optional
import jarrid.keyper.utils.shell.Command
import kotlinx.coroutines.runBlocking

abstract class DeploySubcommand(help: String = "") : BaseSubcommand(help = help) {
    private val cdktfArgs by argument().multiple().optional()
    val cdktfArgsCmd
        get() = cdktfArgs?.joinToString(" ") ?: ""

    val command = Command()

    override fun run() = runBlocking {
        runAsync()
    }

    abstract suspend fun runAsync()
}

class Deploy :
    NoOpCliktCommand(
        help = "Deploy module implements Terraform Cloud Development Kit underneath. It scans existing key configs generated by key module and programmatically construct terraform provider, modules and resources accordingly. For more information, visit: https://jarrid.xyz"
    )
