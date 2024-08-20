package jarrid.keyper.tfcdk.stack.aws

import com.hashicorp.cdktf.Testing
import io.mockk.mockk
import jarrid.keyper.utils.model.NewUUID
import org.junit.jupiter.api.BeforeEach
import software.constructs.Construct

class AWSTest {

    private lateinit var aws: AWS
    private lateinit var scope: Construct
    private val keyId = NewUUID.get()
    private val role = "test-role"

    @BeforeEach
    fun setUp() {
        scope = mockk<Construct>(relaxed = true)
        aws = AWS(Testing.app(), "test-stack")
    }
}