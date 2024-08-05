---
title: Keyper Configuration
hide:
  - toc
---

## Terraform Configuration

### Migrate Terraform Backend to Remote

You can start experimenting with [local Terraform backend](https://developer.hashicorp.com/terraform/language/settings/backends/local){target=_blank}. However, once you are ready to take this project to full staging/production, you can [migrate Terraform state backend to remote](https://developer.hashicorp.com/terraform/tutorials/cloud/cloud-migrate). Follow [Add GCS Permission](./gcp.md#add-gcs-permission) to add GCS permission to the cdktf service account.

```bash
cd cdktf.out/stacks/default
terraform init -migrate-state
```

[WIP]
