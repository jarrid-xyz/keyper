package jarrid.keyper.resource.iam
import jarrid.keyper.utils.file.Backend
import jarrid.keyper.resource.BaseManager
import jarrid.keyper.resource.iam.Model


abstract class Manager(backend: Backend): BaseManager(backend=backend) {
    abstract suspend fun createServiceAccount(): Model
}