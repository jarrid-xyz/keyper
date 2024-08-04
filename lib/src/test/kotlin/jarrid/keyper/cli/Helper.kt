package jarrid.keyper.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context

object Helper {
    fun parseCommand(command: CliktCommand, args: Array<String>) {
        command.context {
            allowInterspersedArgs = false
            autoEnvvarPrefix = "TEST"
        }
        command.parse(args)
    }
}
