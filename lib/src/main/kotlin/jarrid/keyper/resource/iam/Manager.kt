//package jarrid.keyper.resource.iam
//import jarrid.keyper.resource.*
//import jarrid.keyper.utils.file.Backend
//import jarrid.keyper.utils.model.NewTimestamp
//import jarrid.keyper.utils.model.NewUUID
//
//
//abstract class Manager(backend: Backend): BaseManager(backend=backend) {
//    suspend fun convert(payload: Payload, usage: Usage): Model {
//        when (usage) {
//            Usage.CREATE -> {
//                return Model(
//                    resource = ResourceModel(
//                        resource = Resource(
//                            id = NewUUID.get(),
//                            name = payload.base.name,
//                        ),
//                        base = BaseModel(
//                            created = NewTimestamp.get(),
//                            deployment = getDeployment(payload.base.deployment),
//                            context = payload.base.context,
//                        ),
//                    ),
//                )
//            }
//        }
//    }
//
//    abstract suspend fun createServiceAccount(): Model
//}