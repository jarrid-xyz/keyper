package jarrid.keyper.utils.logging

import io.klogging.Level
import io.klogging.config.loggingConfiguration
import io.klogging.rendering.RENDER_SIMPLE
import io.klogging.sending.STDOUT

fun config() {
    loggingConfiguration(append = true) {
        sink("stdout", RENDER_SIMPLE, STDOUT)
        logging {
            fromLoggerBase("jarrid.keyper")
            fromMinLevel(Level.INFO) {
                toSink("stdout")
            }
        }
    }
}