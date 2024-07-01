package jarrid.keyper.utils.shell

import io.klogging.Klogging
import java.io.BufferedReader
import java.io.InputStreamReader

class Command : Klogging {
    private val cdktfPath: String = System.getenv("CDKTF_PATH")

    suspend fun run(command: String, envVars: Map<String, String>? = null): String {
        val builder = ProcessBuilder("/bin/sh", "-c", command)
            .redirectErrorStream(true)

        val env = builder.environment()
        envVars?.let {
            for ((key, value) in it) {
                env[key] = value
            }
        }

        logger.info("Run command: ${builder.command()}, with env: $env")
        val process = builder.start()
        val out = StringBuilder()
        BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                out.append(line)
                logger.info("Output | $line")
            }
        }

        val code = process.waitFor()
        if (code != 0) {
            throw RuntimeException("Command exited with non-zero exit code $code")
        }
        return out.toString()
    }

    suspend fun cdktf(command: String): String {
        val path = run("echo \$PATH")
        val compiled = "cdktf $command"

        return run(compiled, mapOf("PATH" to "$path:$cdktfPath"))
    }
}
