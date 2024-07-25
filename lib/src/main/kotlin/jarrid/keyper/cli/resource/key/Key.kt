package jarrid.keyper.cli.resource.key

import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import jarrid.keyper.resource.BasePayload
import jarrid.keyper.resource.Payload
import jarrid.keyper.resource.key.EditPermission
import jarrid.keyper.resource.iam.Manager as IAMManager
import jarrid.keyper.resource.key.Manager as KeyManager

class RoleNameInvalidException(message: String) : Exception(message)
class RoleNameRequiredException(message: String) : Exception(message)

class Key(help: String = "Manage key resource") : KeySubcommand(help = help) {

    private val operation: EditPermission by option(
        "-o", "--operation", help = "Key permission operation"
    ).enum<EditPermission>().required()

    private val roles: List<String> by option(
        "-r", "--role", help = "Role names"
    ).multiple()

    private suspend fun validate(payload: Payload) {
        if (roles.isEmpty()) {
            throw RoleNameRequiredException("At least one role must be provided.")
        }

        val iamManager = IAMManager(backend.get(), stack)
        val existingRoles = iamManager.list(payload).map { it.base.name ?: it.base.id.toString() }

        val valid = roles.filter { it in existingRoles }
        if (valid.isEmpty()) {
            throw RoleNameInvalidException("No valid roles provided. Run `keyper resource list -t role` to see available roles")
        }
    }

    override suspend fun runAsync() {

        val deployment = BasePayload(
            name = deployment,
        )
        val payload = Payload(
            deployment = deployment,
            resource = BasePayload(id = keyId)
        )
        validate(payload)
        val keyManager = KeyManager(backend.get(), stack)
        keyManager.permission(payload, operation, roles)
    }
}