package jarrid.keyper.cli.deploy

import jarrid.keyper.cli.DeploySubcommand

class Plan :
    DeploySubcommand(
        help =
        "Runs cdktf synth and cdktf diff. Equivalent of terraform plan in cdktf. For more information, visit: https://jarrid.xyz/keyper"
    ) {
    override suspend fun runAsync() {
        command.cdktf("diff $cdktfArgsCmd")
    }
}