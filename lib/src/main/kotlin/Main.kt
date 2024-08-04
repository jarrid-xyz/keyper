import jarrid.keyper.app.Config
import jarrid.keyper.tfcdk.TfApp
import jarrid.keyper.utils.logging.config

/**
 * Default function to run cdktf synth
 */
suspend fun main() {
    config()
    val app = TfApp(Config().get())
    app.run()
}
