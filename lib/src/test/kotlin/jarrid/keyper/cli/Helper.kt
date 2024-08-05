package jarrid.keyper.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import kotlinx.coroutines.runBlocking
import java.io.ByteArrayOutputStream
import java.io.PrintStream

object Helper {
    fun parseCommand(command: CliktCommand, args: Array<String>) {
        command.context {
            allowInterspersedArgs = false
            autoEnvvarPrefix = "TEST"
        }
        command.parse(args)
    }

    fun captureStdOutSuspend(block: suspend () -> Unit): String = runBlocking {
        val outputStream = ByteArrayOutputStream()
        val originalOut = System.out
        System.setOut(PrintStream(outputStream))
        try {
            block()
        } finally {
            System.setOut(originalOut)
        }
        return@runBlocking outputStream.toString().trim()
    }
}
