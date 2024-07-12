---
title: Home
hide:
  - toc
---

## About

[Keyper](https://github.com/apiobuild/jarrid-keyper/){:target="_blank"} by [Jarrid](https://jarrid.xyz){:target="_blank"} is a suite of crypto key management APIs to simplify key creation, management, deployment, encryption/decryption in a standardized and secure way. Operations are file-based and can be easily automated, tracked, audited, and managed via file-based processes such as [GitOps](https://github.com/topics/gitops){:target="_blank"}. Fully integrated with cloud KMS services such as [AWS KMS](https://docs.aws.amazon.com/kms){:target="_blank"} or [GCP KMS](https://cloud.google.com/kms){:target="_blank"}, leverage managed crypto key generation and reduce infrastructure maintenance burden.

The library has three main modules:

1. [Crypto Key](crypto-key/index.md): Create key configs as JSON files. Configuration helps you manage key implementations in a simple, trackable, and readable way.
2. [Deploy](deploy/index.md): Take the existing key configs (in JSON files), plan and deploy via [Terraform](https://www.terraform.io/){:target="_blank"} accordingly. Take advantage of Terraform's existing functionalities such as state management, dependency resolution, and drift tracking without losing the flexibility to modularize deployment as granular as needed.
3. [Data](data/index.md): Run various encrypt and decrypt data flows with pre-defined keys. Adjust the level of security depending on use cases without additional implementation.

## Demo

In only three steps, you can create a key, deploy the key on cloud KMS, and encrypt/decrypt data. Everything is wrapped and managed in [Keyper](https://github.com/apiobuild/jarrid-keyper/){:target="_blank"}.

1. **Create Key**
   
      ```bash
      keyper create key --backend local --stack gcp
      ```

      <script src="https://asciinema.org/a/667093.js" id="asciicast-667093" async="true"></script>

2. **Deploy via [Terraform](https://www.terraform.io/){:target="_blank"}**

      ```bash
      keyper deploy apply
      ```

      <script src="https://asciinema.org/a/667095.js" id="asciicast-667095" async="true"></script>

3. **Encrypt/Decrypt**

      ```bash
      keyper data encrypt --backend local --stack gcp --key-id <> --plaintext <>
      ```

      ```bash
      keyper data decrypt --backend local --stack gcp --key-id <> --ciphertext <>
      ```

      <script src="https://asciinema.org/a/667096.js" id="asciicast-667096" async="true"></script>

## Releases

Keyper publish release every Monday. Stay updated with our latest developments by checking out our [release notes](https://github.com/apiobuild/jarrid-keyper/releases){:target="_blank"}. If you have a specific feature in mind that you'd like to see implemented, feel free to submit a [feature request](https://github.com/apiobuild/jarrid-keyper/issues/new/choose){:target="_blank"}. You can also [track the progress](https://github.com/orgs/apiobuild/projects/5/views/1){:target="_blank"} of ongoing feature requests and see what's coming next.

## Need Help?

We are a small but ambitious team actively looking to expand our capabilities. We'd love to learn about your use cases and feedback. If you need help with implementation or have questions about crypto key or data and software security in general, don't hesitate to reach out.

[Contact Us](https://jarrid.xyz/#contact){ .md-button .md-button--primary target="_blank" } [Try Now](https://github.com/apiobuild/jarrid-keyper/){ .md-button .md-button--primary target="_blank" }
