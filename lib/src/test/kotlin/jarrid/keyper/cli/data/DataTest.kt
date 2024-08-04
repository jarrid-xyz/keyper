package jarrid.keyper.cli.data

import io.mockk.*
import jarrid.keyper.cli.Helper
import jarrid.keyper.resource.Backend
import jarrid.keyper.resource.Deployment
import jarrid.keyper.resource.Model
import jarrid.keyper.resource.Stack

import jarrid.keyper.utils.model.NewUUID
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import jarrid.keyper.resource.key.Model as Key
import jarrid.keyper.resource.key.data.Decrypt as KeyDecrypt
import jarrid.keyper.resource.key.data.Encrypt as KeyEncrypt

class DataTest {

    private lateinit var backend: Backend
    private lateinit var stack: Stack
    private lateinit var keyEncryptor: KeyEncrypt
    private lateinit var keyDecryptor: KeyDecrypt
    private lateinit var encryptCommand: Encrypt
    private lateinit var decryptCommand: Decrypt
    private val keyId: UUID = NewUUID.getEmpty()

    @BeforeEach
    fun setUp() {
        backend = mockk()
        stack = mockk()
        keyEncryptor = mockk()
        keyDecryptor = mockk()
        encryptCommand = spyk(Encrypt())
        decryptCommand = spyk(Decrypt())


        // Mock the getEncryptor method to return the mocked encryptor
        encryptCommand = spyk(Encrypt()) {
            every { getEncryptor(any(), any(), any()) } returns keyEncryptor
        }

        // Mock the getDecryptor method to return the mocked decryptor
        decryptCommand = spyk(Decrypt()) {
            every { getDecryptor(any(), any(), any()) } returns keyDecryptor
        }
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun testEncrypt() = runBlocking {
        val plaintext = "testPlaintext"
        val encryptedValue = "testEncryptedValue"
        val args = arrayOf("--plaintext", plaintext, "--key-id", keyId.toString())

        val deployment = Deployment(_name = "testDeployment")
        val key = Key(id = keyId)
        val payload = Model(resource = key, deployment = deployment)

//        every { backend.getDeployment(any()) } returns deployment
        coEvery { keyEncryptor.run(plaintext) } returns encryptedValue

        // Ensure the command arguments are correctly set up
        Helper.parseCommand(encryptCommand, args)

        // Capture the output
        val output = Helper.captureStdOutSuspend {
            encryptCommand.runAsync()
        }

        // Verify the output
        assertEquals("Encrypted value: $encryptedValue", output)

        // Verify the encryptor was called with the correct arguments
        coVerify { keyEncryptor.run(plaintext) }
    }

    @Test
    fun testDecrypt() = runBlocking {
        val ciphertext = "testCiphertext"
        val decryptedValue = "testDecryptedValue"
        val args = arrayOf("--ciphertext", ciphertext, "--key-id", keyId.toString())

        val deployment = Deployment(_name = "testDeployment")
        val key = Key(id = keyId)
        val payload = Model(resource = key, deployment = deployment)

//        every { backend.getDeployment(any()) } returns deployment
        coEvery { keyDecryptor.run(ciphertext) } returns decryptedValue

        // Mock the getDecryptor method to return the mocked decryptor
        every { decryptCommand.getDecryptor(backend, stack, payload) } returns keyDecryptor

        // Ensure the command arguments are correctly set up
        Helper.parseCommand(decryptCommand, args)

        // Capture the output
        val output = Helper.captureStdOutSuspend {
            decryptCommand.runAsync()
        }

        // Verify the output
        assertEquals("Decrypted value: $decryptedValue", output)

        // Verify the decryptor was called with the correct arguments
        coVerify { keyDecryptor.run(ciphertext) }
    }
}