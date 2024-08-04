package jarrid.keyper.utils.file

import jarrid.keyper.app.Config
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class Local(config: Config) : Backend(config = config) {
    private val rootDir: Path = Paths.get(root)

    private fun resolve(path: String): Path {
        return rootDir.resolve(path)
    }

    override fun exists(path: String): Boolean {
        val resolved = resolve(path)
        return Files.exists(resolved)
    }

    override fun createDir(path: String) {
        val resolved = resolve(path)
        Files.createDirectories(resolved)
    }

    override fun write(path: String, encoded: String) {
        val resolved = rootDir.resolve(path)
        Files.createDirectories(resolved.parent)
        Files.writeString(resolved, encoded)
    }

    override fun ls(dir: String): List<String> {
        val dirPath = rootDir.resolve(dir)
        if (!Files.isDirectory(dirPath)) {
            throw DirectoryNotFoundException("Dir: $dir doesn't exist or is not a directory. Root dir: $rootDir")
        }
        return Files.list(dirPath).map { it.fileName.toString() }.toList()
    }

    override fun read(path: String): String {
        val resolved = resolve(path)
        if (!Files.exists(resolved)) {
            throw ResourceNotFoundException("Resource not found: $path")
        }
        return Files.readString(resolved)
    }
}