import jarrid.keyper.tfcdk.App
import jarrid.keyper.utils.logging.config

/**
 * Default function to run cdktf synth
 */
suspend fun main() {
    config()
    val app = App()
    app.run()
}
