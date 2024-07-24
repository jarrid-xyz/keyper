package jarrid.keyper.cli.deploy

import jarrid.keyper.cli.DeploySubcommand

class Apply :
    DeploySubcommand(help =
        "Runs cdktf synth and cdktf deploy. Equivalent of terraform apply in cdktf. " +
        "For more information, visit: https://jarrid.xyz"
    ) {
    override suspend fun runAsync() {
        command.cdktf("deploy --auto-approve $cdktfArgsCmd")
    }
}