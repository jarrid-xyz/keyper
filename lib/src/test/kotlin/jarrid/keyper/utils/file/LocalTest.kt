package jarrid.keyper.utils.file

import io.mockk.every
import io.mockk.mockk
import jarrid.keyper.resource.App
import jarrid.keyper.resource.BackendConfig
import jarrid.keyper.resource.Config
import jarrid.keyper.resource.ResourceBackend
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class LocalTest {

    private lateinit var local: Local
    private lateinit var rootDir: Path
    private lateinit var dirPath: Path

    @BeforeEach
    fun setup() {
        // Create a temporary directory for the root
        rootDir = Files.createTempDirectory("root")

        val config = mockk<Config>()
        every { config.get() } returns App(
            outDir = rootDir.toString(),
            resource = ResourceBackend(
                backend = BackendConfig(
                    path = "dir"
                )
            )
        )

        local = Local(config)
        dirPath = rootDir.resolve("dir")
        Files.createDirectories(dirPath)
    }

    @Test
    fun `test exists`() {
        val existingFilePath = dirPath.resolve("existing_file.txt")
        Files.createFile(existingFilePath)

        assertTrue(local.exists("dir/existing_file.txt"))
        assertTrue(!local.exists("dir/non_existing_file.txt"))
    }

    @Test
    fun `test createDir`() {
        val newDirPath = dirPath.resolve("new_dir")

        local.createDir("dir/new_dir")

        assertTrue(Files.exists(newDirPath))
        assertTrue(Files.isDirectory(newDirPath))
    }

    @Test
    fun `test write`() {
        val filePath = dirPath.resolve("file.txt")
        val content = "Hello, World!"

        local.write("dir/file.txt", content)

        assertTrue(Files.exists(filePath))
        assertEquals(content, Files.readString(filePath))
    }

    @Test
    fun `test ls`() {
        val file1 = dirPath.resolve("file1.txt")
        val file2 = dirPath.resolve("file2.txt")
        Files.createFile(file1)
        Files.createFile(file2)

        val files = local.ls("dir")

        assertTrue(files.contains("file1.txt"))
        assertTrue(files.contains("file2.txt"))
        assertEquals(2, files.size)
    }

    @Test
    fun `test ls non-existent directory`() {
        assertFailsWith<DirectoryNotFoundException> {
            local.ls("non_existent_dir")
        }
    }

    @Test
    fun `test read`() {
        val filePath = dirPath.resolve("file.txt")
        val content = "Hello, World!"
        Files.writeString(filePath, content)

        val readContent = local.read("dir/file.txt")

        assertEquals(content, readContent)
    }

    @Test
    fun `test read non-existent file`() {
        assertFailsWith<ResourceNotFoundException> {
            local.read("dir/non_existent_file.txt")
        }
    }
}