package jarrid.keyper.resource.key

import jarrid.keyper.resource.Base

object Name {
    fun getJarridKeyName(key: Base): String {
        return key.name ?: "jarrid-keyper-key-${key.id}"
    }

    fun getSanitizedAccountId(name: String): String {
        // Step 1: Convert to lowercase
        var cleaned = name.lowercase()
        // Step 2: Remove invalid characters
        cleaned = cleaned.replace("[^a-z0-9-]".toRegex(), "")
        // Step 3: Ensure the string starts with a lowercase letter
        cleaned = cleaned.dropWhile { it !in 'a'..'z' }
        // Step 4: Ensure the string ends with a lowercase letter or digit
        cleaned = cleaned.dropLastWhile { it !in 'a'..'z' && it !in '0'..'9' }
        // Step 5: Remove consecutive hyphens
        cleaned = cleaned.replace("--+".toRegex(), "-")

        return cleaned
    }
}