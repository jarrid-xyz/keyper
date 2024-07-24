---
title: Resource
hide:
  - navigation
---

# About

The Resource Module generates key and service configurations in JSON format based on specified usage. This module
currently supports
using local disk as the file backend. Future enhancements plan to include support for remote storage options such
as [GitHub](https://github.com/){:target="_blank"}, [S3](https://aws.amazon.com/s3/){:target="_blank"},
and [GCS](https://cloud.google.com/storage){:target="_blank"}.

The key configuration is intended flexible and trackable. Config files will be used for both deployment and data
encryption and decryption.

## Key

Use the CLI to create and manage key configurations in JSON.

Run

```bash
keyper resource create -t key
```

This will create a key configuration file in the `configs/<deployment name>/key/<uuid>` directory:

```json
{
  "base": {
    "created": "<timestamp>",
    "id": "<uuid>"
  },
  "type": "KEY"
}
```

You can also use the CLI to list keys:

```bash
keyper resource list -t key

# Keys: 68618138-019a-4fb8-ab98-93aa07c0c984, b0b0034b-4f7b-46a8-8920-8a234503c25f
```

## Role

```bash
keyper resource create -t role
```

This will create a role configuration file in the `configs/<deployment name>/role/<uuid>` directory:

```json
{
  "base": {
    "created": "<timestamp>",
    "id": "<uuid>",
    "name": "test-role"
  },
  "type": "ROLE"
}
```

You can also use the CLI to list roles:

```bash
keyper resource list -t role

Roles: test-role
```

## Demo

```bash
keyper resource create -t key 
```

```bash
keyper resource create -t role
```

<script src="https://asciinema.org/a/667093.js" id="asciicast-667093" async="true"></script>
