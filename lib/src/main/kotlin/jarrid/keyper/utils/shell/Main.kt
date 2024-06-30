package jarrid.keyper.utils.shell

import java.io.BufferedReader
import java.io.InputStreamReader

fun runCommand(command: String): String {
    val process = ProcessBuilder(*command.split(" ").toTypedArray())
        .redirectErrorStream(true)
        .start()
    val output = StringBuilder()
    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            output.append(line).append("\n")
        }
    }

    process.waitFor()
    return output.toString()
}