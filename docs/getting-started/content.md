1. Pull [Keyper's pre-packaged docker images](https://github.com/jarrid-xyz/keyper/pkgs/container/keyper){:target="_blank"}: `ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}}`

    ``` bash
    docker pull ghcr.io/jarrid-xyz/keyper:{{config.theme.extra.version}}
    ```

2. Run a help command:
   
   ```bash
   docker run -it --rm --name keyper-cli \
    -v ./configs:/home/keyper/configs \
    -v ./cdktf.out:/home/keyper/cdktf.out \
    -v ./.cdktf-sa-key.json:/home/keyper/gcp.json \
    -v ./app.local.yaml:/home/keyper/app.local.yaml
    ghcr.io/jarrid-xyz/keyper:v0.0.1 -h
   ```
