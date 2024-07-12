---
title: CLI
---

Keyper deploy CLI wraps cdktf commands for streamlined deployment processes.

### Plan

This wraps the [`cdktf synth`](https://developer.hashicorp.com/terraform/cdktf/cli-reference/commands#synth){:target="_blank"} and [`cdktf diff`](https://developer.hashicorp.com/terraform/cdktf/cli-reference/commands#diff){:target="_blank"} commands underneath. It's the equivalent of [`terraform plan`](https://developer.hashicorp.com/terraform/cli/commands/plan){:target="_blank"} in [cdktf](https://developer.hashicorp.com/terraform/cdktf){:target="_blank"}.

```bash
keyper deploy plan # add <args> if needed
```

This will create tf plans for review. This step is typically called in CI pipeline to ensure proposed resource change is acceptable.

### Apply

This wraps [`cdktf deploy`](https://developer.hashicorp.com/terraform/cdktf/cli-reference/commands#deploy){:target="_blank"} command
underneath. It's the equivalent of [`terraform apply`](https://developer.hashicorp.com/terraform/cli/commands/apply){:target="_blank"}
in [cdktf]((https://developer.hashicorp.com/terraform/cdktf)){:target="_blank"}.

```bash
keyper deploy apply # add <args> if needed
```

This will create apply tf plans. This step is typically called in CD pipeline to apply the proposed resource change.