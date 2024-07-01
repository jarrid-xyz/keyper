package jarrid.keyper.api

import io.klogging.Klogging
import jarrid.keyper.tfcdk.Stack

class TfServiceImpl(options: Options) : Klogging, Service(options) {
    private val tf = Stack(backend, stack)

    suspend fun diff() {
        tf.run()
//        val command = "cdktf diff"
//        logger.info("Running cdktf command: $command")
//        val out = runCommand(command)
//        logger.info(out)
    }
}