---
title: Data
hide:
    - toc
---

The data module allows you to manage data with crypto keys created by [Keyper](https://github.com/apiobuild/jarrid-keyper/){:target="_blank"}.

## Encrypt and Decrypt Data

Encrypt and decrypt data can be as simple as two commands:

```bash
keyper data encrypt --backend LOCAL --stack GCP --key-id "<key-id>" --plaintext "<>"
# should return: Encrypted value: <...>
keyper data decrypt --backend LOCAL --stack GCP --key-id "<key-id>" --ciphertext "<>"
# should return: Decrypted value: <...>
```
