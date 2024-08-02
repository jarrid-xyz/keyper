package jarrid.keyper.cli.deploy

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import jarrid.keyper.cli.DeploySubcommand
import jarrid.keyper.utils.shell.Command
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

object TestHelper {
    fun parseCommand(command: CliktCommand, args: Array<String>) {
        command.context {
            allowInterspersedArgs = false
            autoEnvvarPrefix = "TEST"
        }
        command.parse(args)
    }
}

class DeployCommandTests {

    private lateinit var command: Command

    @BeforeEach
    fun setUp() {
        command = mockk()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    data class CommandTestCase(
        val command: CliktCommand,
        val args: Array<String>,
        val expected: String
    )

    companion object {
        @JvmStatic
        fun commandTestCases(): List<CommandTestCase> {
            return listOf(
                // Apply command test cases
                CommandTestCase(
                    command = Apply().apply { command = mockk() },
                    args = arrayOf(),
                    expected = "deploy --auto-approve "
                ),
                CommandTestCase(
                    command = Apply().apply { command = mockk() },
                    args = arrayOf("arg1"),
                    expected = "deploy --auto-approve arg1"
                ),
                CommandTestCase(
                    command = Apply().apply { command = mockk() },
                    args = arrayOf("arg1", "arg2", "arg3"),
                    expected = "deploy --auto-approve arg1 arg2 arg3"
                ),
                // Plan command test cases
                CommandTestCase(
                    command = Plan().apply { command = mockk() },
                    args = arrayOf(),
                    expected = "diff "
                ),
                CommandTestCase(
                    command = Plan().apply { command = mockk() },
                    args = arrayOf("arg1"),
                    expected = "diff arg1"
                ),
                CommandTestCase(
                    command = Plan().apply { command = mockk() },
                    args = arrayOf("arg1", "arg2", "arg3"),
                    expected = "diff arg1 arg2 arg3"
                ),
                // Destroy command test cases
                CommandTestCase(
                    command = Destroy().apply { command = mockk() },
                    args = arrayOf(),
                    expected = "destroy --auto-approve "
                ),
                CommandTestCase(
                    command = Destroy().apply { command = mockk() },
                    args = arrayOf("arg1"),
                    expected = "destroy --auto-approve arg1"
                ),
                CommandTestCase(
                    command = Destroy().apply { command = mockk() },
                    args = arrayOf("arg1", "arg2", "arg3"),
                    expected = "destroy --auto-approve arg1 arg2 arg3"
                )
            )
        }
    }

    @ParameterizedTest
    @MethodSource("commandTestCases")
    fun testCommand(case: CommandTestCase) = runBlocking {
        val mock: Command = (case.command as DeploySubcommand).command
        coEvery { mock.cdktf(case.expected) } returns "Executed successfully"

        // Ensure the command arguments are correctly set up
        TestHelper.parseCommand(case.command, case.args)

        // Run the command
        case.command.main(case.args)

        // Verify the command was called as expected
        coVerify { mock.cdktf(case.expected) }
    }
}