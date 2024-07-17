package jarrid.keyper.api

import jarrid.keyper.tfcdk.KeyStack
import jarrid.keyper.utils.file.Backend
import kotlin.reflect.KClass
import jarrid.keyper.resource.Backend as BackendEnum
import jarrid.keyper.resource.Stack as StackEnum

data class Options(
    val backend: BackendEnum,
    val stack: StackEnum
)

abstract class Service(
    options: Options
) {
    val backend: Backend = options.backend.get()
    val stack: KClass<out KeyStack> = options.stack.get()
}