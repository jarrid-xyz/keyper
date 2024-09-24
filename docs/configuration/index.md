---
title: Configuration
hide:
  - navigation
---

## Keyper Configuration

[We've preset keyper with `app.yaml` file here](https://github.com/jarrid-xyz/keyper/blob/main/lib/src/main/resources/app.yaml); however, you can override it with `app.<env>.yaml`.

### Options

```yaml
provider:                      # terraform provider options
  tfcdk:                       # tfcdk options
    stack: "gcp"               # deploy to aws or gcp
    path: "cdktf.out"          # write cdktf artifact to directory
  aws:                         # aws provider options
    region: us-east-1          # provider region
    backend:                   # provider backend options
      type: cloud              # use local or cloud backend, use s3 if cloud
      path: terraform.tfstate  # tfstate path (for both local and cloud)
      bucket: keyper-tf-state  # bucket to use (for cloud)
    assume_role_arn:           # assume role arn to use for tf aws provider (optional)
  gcp:                         # gcp provider options
    accountId: "databoo"       # GCP project id
    region: us-east1           # provider region
    credentials:               # path to provider credentials
    backend:                   # provider backend options
      type: cloud              # use local or cloud backend, use gcs if cloud
resource:                      # resource json configurations files options
  backend:                     # backend options
    backend: "local"           # backend currently only supports local
    path: "configs"            # write json configurations files to directory

env: local                     # provide additional app.<env>.yaml override options
out_dir: "/home/keyper"        # base path of both provider.tfcdk.path and resource.backend.path
```

### Docker

If you are using docker image, you can override `app.local.yaml` via `-v ./app.local.yaml:/home/keyper/app.local.yaml`.

```bash
docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml \
    ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}} -h
```

### Jar

If you are building jar, you can create a `app.yaml` in the root directory.


## Terraform Configuration

### Migrate Terraform Backend to Remote

You can start experimenting with [local Terraform backend](https://developer.hashicorp.com/terraform/language/settings/backends/local){target=_blank}. However, once you are ready to take this project to full staging/production, you can [migrate Terraform state backend to remote](https://developer.hashicorp.com/terraform/tutorials/cloud/cloud-migrate). Follow [Add GCS Permission](./gcp.md#add-gcs-permission) to add GCS permission to the cdktf service account.

```bash
cd cdktf.out/stacks/default
terraform init -migrate-state
```
