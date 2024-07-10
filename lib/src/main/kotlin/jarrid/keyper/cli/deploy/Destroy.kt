package jarrid.keyper.cli.deploy

import jarrid.keyper.cli.DeploySubcommand

class Destroy :
    DeploySubcommand(help = "Runs cdktf synth and cdktf destroy. Equivalent of terraform destroy in cdktf. For more information, visit: https://jarrid.xyz") {
    override suspend fun runAsync() {
        command.cdktf("destroy --auto-approve $cdktfArgsCmd")
    }
}