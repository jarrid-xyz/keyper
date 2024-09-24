### Tutorial

For full Keyper tutorial, you can find it here: [https://github.com/jarrid-xyz/keyper-tutorial](https://github.com/jarrid-xyz/keyper-tutorial){:target="_blank"}

### Keyper Github Action

We've created the [Keyper Github Action](https://github.com/jarrid-xyz/keyper-action){:target="_blank"} to automate Keyper deployment using [GitOps](https://github.com/topics/gitops){:target="_blank"} flow. This makes Keyper resource management fully configuration-driven. Both technical and non-technical teams can either edit the configuration files directly or use the [Keyper CLI](#keyper-docker-cli) to manage resources and the [Keyper Github Action](https://github.com/jarrid-xyz/keyper-action){:target="_blank"} will handle the rest of the CI/CD process.

➡️ [Go to Keyper Github Action Tutorial](https://github.com/jarrid-xyz/keyper-tutorial/tree/main/6-use-cases/6-4-deploy-keyper-via-github-action){:target="_blank"}.

The easiest way to set it up is to copy our [example workflow](https://github.com/jarrid-xyz/keyper-tutorial/blob/main/.github/workflows/keyper-cicd.yml){:target="_blank"} into your own repository and modify the configurations accordingly:

```yaml
name: Keyper Action (Deploy Plan/Apply)

on:
  push:
    branches: [main]
  pull_request:
    branches: [main]

jobs:
  keyper-action:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Run Keyper Action (Deploy Plan)
        id: keyper-plan
        uses: jarrid-xyz/keyper@{{config.theme.extra.version}}
        with:
          args: deploy plan
      - name: Run Keyper Action (Deploy Apply)
        id: keyper-apply
        uses: jarrid-xyz/keyper@{{config.theme.extra.version}}
        with:
          args: deploy apply
        if: github.ref == 'refs/heads/main' # Only run if merge to main
```

### Keyper Docker CLI

#### Pull Docker Image

Pull [Keyper's pre-packaged docker images](https://github.com/jarrid-xyz/keyper/pkgs/container/keyper){:target="_blank"}: `ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}}`

``` bash
docker pull ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}}
```

#### Create App Configuration and Credentials

1. Follow [Keyper Configuration](../deploy/configuration.md) to create `app.<env>.yaml` to configure Terraform provider and backend accordingly.

2. Follow [Create GCP KMS Admin Service Account](../deploy/gcp.md#create-resource-admin-service-account) to create `.cdktf-sa-key.json`. This service account credential is needed to create actual resources via Terraform.

### Run Keyper Command
   
Validate that docker image is working properly.

```bash
docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml \
    ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}} -h
```

#### Create Deployment, Role and Key

Create the resource configurations locally.

```bash
docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml \
    ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}} resource create -t deployment
```

```bash
docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml \
    ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}} resource create -t role -n app-role
```

```bash
docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml \
    ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}} resource create -t key
```

#### Deploy via Terraform

Provision resource on the cloud based on the resource configurations.

```bash
docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml \
    ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}} deploy apply
```

#### Encrypt/Decrypt Data with Key

```bash
docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml \
    ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}} data encrypt -k <key-id> --plaintext <secret>
```

```bash
docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml \
    ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}} data decrypt -k <key-id> --ciphertext <secret>
```

You just successfully use KMS key to encrypt/decrypt data. :tada: 
