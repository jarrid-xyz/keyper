---
title: Resource
hide:
  - toc
---

The Resource Module generates resource configurations in JSON format based on specified usage. This module
currently supports using local disk as the file backend. Future enhancements plan to include support for 
remote storage options such as [GitHub](https://github.com/){:target="_blank"}, 
[S3](https://aws.amazon.com/s3/){:target="_blank"}, and [GCS](https://cloud.google.com/storage){:target="_blank"}.

The resource configuration is intended flexible and trackable. Config files will be used for both deployment and data
encryption and decryption. While [Keyper](https://github.com/jarrid-xyz/keyper/){:target="_blank"} has CLI to generate 
and validate configurations, it's entirely possible to manage configurations through text editor and JSON linter.

## Demo

```bash
keyper resource create -t key 
keyper resource create -t role
```

<script src="https://asciinema.org/a/669368.js" id="asciicast-669368" async="true"></script>
