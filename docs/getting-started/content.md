## Tutorial

For end to end tutorial, you can find it here: [https://github.com/jarrid-xyz/keyper-tutorial](https://github.com/jarrid-xyz/keyper-tutorial){:target="_blank"}

## Quick Start

### Pull Docker Image

Pull [Keyper's pre-packaged docker images](https://github.com/jarrid-xyz/keyper/pkgs/container/keyper){:target="_blank"}: `ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}}`

``` bash
docker pull ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}}
```

### Create App Configuration and Credentials

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

### Create Deployment, Role and Key

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

### Deploy via Terraform

Provision resource on the cloud based on the resource configurations.

```bash
docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml \
    ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}} deploy apply
```

### Encrypt/Decrypt Data with Key

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
