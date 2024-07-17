import jarrid.keyper.resource.Backend.LOCAL
import jarrid.keyper.tfcdk.KeyStack
import jarrid.keyper.tfcdk.gcp.stack.GCPKeyStackImpl
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.utils.logging.config
import kotlin.reflect.KClass

/**
 * Default function to run cdktf synth
 */
suspend fun main() {
    config()
    val backend: Backend = LOCAL.get()
    val stack: KClass<out KeyStack> = GCPKeyStackImpl::class
    val tf = jarrid.keyper.tfcdk.Stack(backend, stack)
    tf.run()
}
