---
title: Crypto Key
hide:
  - navigation
---

# About

The Key Module generates key configurations in JSON format based on specified usage. This module currently supports using local disk as the file backend. Future enhancements plan to include support for remote storage options such as [GitHub](https://github.com/){:target="_blank"}, [S3](https://aws.amazon.com/s3/){:target="_blank"}, and [GCS](https://cloud.google.com/storage){:target="_blank"}.

The key configuration is intended flexible and trackable. Config files will be used for both deployment and data encryption and decryption.

## Key Management CLI

Use the CLI to create and manage key configurations in JSON.

Run

```bash
keyper key create --backend LOCAL --stack GCP
```

This will create a key configuration file in the `configs` directory:

```json
{
    "usage":"CREATE_KEY",
    "keyId":"<uuid>",
    "created":"<timestamp>",
    "deploymentId":"<uuid>",
    "context":{}
}
```


You can also use the CLI to list keys:

```bash
keyper key list

# DeploymentId: <uuid>
# Key: [Model(usage=CREATE_KEY, keyName=null, keyId=<uuid>, ttl=7, created=<timestamp>, updated=null, deploymentId=<uuid>, context={})]
```

## Demo

```bash
keyper key create --backend LOCAL --stack GCP
```

<script src="https://asciinema.org/a/667093.js" id="asciicast-667093" async="true"></script>
